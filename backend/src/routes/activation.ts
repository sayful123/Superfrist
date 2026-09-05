import { Router, Request, Response } from 'express';
import jwt from 'jsonwebtoken';
import { query } from '../database';

const router = Router();
const JWT_SECRET = process.env.JWT_SECRET || 'super_frist_vpn_production_secret_key_2026';

// 1. Verify Activation Code
router.post('/verify', async (req: Request, res: Response) => {
  const { code, deviceId } = req.body;

  if (!code || typeof code !== 'string') {
    return res.status(400).json({ success: false, error: 'Activation code is required.' });
  }

  const cleanCode = code.trim().toUpperCase();

  try {
    const result = await query('SELECT * FROM activation_codes WHERE code = $1', [cleanCode]);

    if (result.rows.length === 0) {
      return res.status(404).json({ success: false, error: 'Invalid Activation Code.' });
    }

    const item = result.rows[0];
    const now = Date.now();

    if (item.status === 'DISABLED') {
      return res.status(403).json({ success: false, error: 'Activation Code Disabled.' });
    }

    if (item.status === 'REVOKED') {
      return res.status(403).json({ success: false, error: 'Activation Code Revoked.' });
    }

    if (item.expiry_date && new Date(item.expiry_date).getTime() <= now) {
      return res.status(400).json({ success: false, error: 'Activation Code Expired.' });
    }

    if (item.device_id && deviceId && item.device_id !== deviceId) {
      return res.status(409).json({ success: false, error: 'Code Already Activated on Another Device.' });
    }

    return res.json({
      success: true,
      message: 'Code is valid and ready for activation.',
      data: {
        code: item.code,
        status: item.status,
        validityDays: item.validity_days
      }
    });
  } catch (error: any) {
    console.error('Verify error:', error);
    return res.status(500).json({ success: false, error: 'Server database verification error.' });
  }
});

// 2. Activate Code & Bind to Device
router.post('/activate', async (req: Request, res: Response) => {
  const { code, deviceId, deviceModel } = req.body;

  if (!code || !deviceId) {
    return res.status(400).json({ success: false, error: 'Code and deviceId are required.' });
  }

  const cleanCode = code.trim().toUpperCase();

  try {
    const result = await query('SELECT * FROM activation_codes WHERE code = $1 FOR UPDATE', [cleanCode]);

    if (result.rows.length === 0) {
      return res.status(404).json({ success: false, error: 'Invalid Activation Code.' });
    }

    const item = result.rows[0];
    const now = Date.now();

    if (item.status === 'DISABLED' || item.status === 'REVOKED') {
      return res.status(403).json({ success: false, error: 'Activation Code Disabled.' });
    }

    if (item.expiry_date && new Date(item.expiry_date).getTime() <= now) {
      return res.status(400).json({ success: false, error: 'Activation Code Expired.' });
    }

    if (item.device_id && item.device_id !== deviceId) {
      return res.status(409).json({ success: false, error: 'Code Already Activated on Another Device.' });
    }

    // Calculate dates
    const activationDate = item.activation_date ? new Date(item.activation_date) : new Date();
    const expiryDate = item.expiry_date 
      ? new Date(item.expiry_date) 
      : new Date(activationDate.getTime() + (item.validity_days * 86400000));
    const userId = item.user_id || `USR-${cleanCode.slice(-4)}`;

    await query(
      `UPDATE activation_codes 
       SET status = 'USED', device_id = $1, user_id = $2, activation_date = $3, expiry_date = $4, last_connection = NOW()
       WHERE code = $5`,
      [deviceId, userId, activationDate, expiryDate, cleanCode]
    );

    // Audit log
    await query(
      `INSERT INTO audit_logs (action, target, details) VALUES ($1, $2, $3)`,
      ['CODE_ACTIVATED', cleanCode, `Bound to device ${deviceId} (${deviceModel || 'Mobile'})`]
    );

    // Generate JWT Token
    const token = jwt.sign(
      { userId, deviceId, code: cleanCode, role: 'USER' },
      JWT_SECRET,
      { expiresIn: `${item.validity_days}d` }
    );

    // Fetch active VPN servers
    const serversRes = await query('SELECT * FROM vpn_servers WHERE is_visible = true AND status = $1', ['ONLINE']);

    return res.json({
      success: true,
      message: 'Activation successful.',
      data: {
        token,
        userId,
        code: cleanCode,
        deviceId,
        activationDate: activationDate.getTime(),
        expiryDate: expiryDate.getTime(),
        serverTime: Date.now(),
        status: 'USED',
        validityDays: item.validity_days,
        serverList: serversRes.rows
      }
    });
  } catch (error: any) {
    console.error('Activate error:', error);
    return res.status(500).json({ success: false, error: 'Database execution error during activation.' });
  }
});

// 3. User Status
router.get('/status', async (req: Request, res: Response) => {
  const authHeader = req.headers.authorization;
  if (!authHeader) {
    return res.status(401).json({ success: false, error: 'Missing authorization token.' });
  }

  const token = authHeader.replace('Bearer ', '');
  try {
    const decoded: any = jwt.verify(token, JWT_SECRET);
    const result = await query('SELECT * FROM activation_codes WHERE code = $1', [decoded.code]);

    if (result.rows.length === 0) {
      return res.status(404).json({ success: false, error: 'Session code not found.' });
    }

    const item = result.rows[0];
    const now = Date.now();
    const expiryTimestamp = new Date(item.expiry_date).getTime();
    const isExpired = expiryTimestamp <= now;
    const isDisabled = item.status === 'DISABLED' || item.status === 'REVOKED';

    return res.json({
      success: true,
      data: {
        userId: item.user_id,
        code: item.code,
        status: item.status,
        expiryDate: expiryTimestamp,
        serverTime: now,
        remainingSeconds: Math.max(0, Math.floor((expiryTimestamp - now) / 1000)),
        isExpired,
        isDisabled
      }
    });
  } catch (err) {
    return res.status(401).json({ success: false, error: 'Invalid or expired session token.' });
  }
});

export default router;
