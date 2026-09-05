import { Router, Request, Response } from 'express';
import bcrypt from 'bcrypt';
import jwt from 'jsonwebtoken';
import { query } from '../database';

const router = Router();
const JWT_SECRET = process.env.JWT_SECRET || 'super_frist_vpn_production_secret_key_2026';

// Middleware for Admin authentication
const requireAdmin = (req: Request, res: Response, next: Function) => {
  const authHeader = req.headers.authorization;
  if (!authHeader) {
    return res.status(401).json({ success: false, error: 'Missing admin credentials.' });
  }

  const token = authHeader.replace('Bearer ', '');
  try {
    const decoded: any = jwt.verify(token, JWT_SECRET);
    if (decoded.role !== 'SUPER_ADMIN') {
      return res.status(403).json({ success: false, error: 'Forbidden: Admin privilege required.' });
    }
    (req as any).admin = decoded;
    next();
  } catch (e) {
    return res.status(401).json({ success: false, error: 'Invalid or expired admin token.' });
  }
};

// 1. Admin Login
router.post('/login', async (req: Request, res: Response) => {
  const { email, password } = req.body;

  if (!email || !password) {
    return res.status(400).json({ success: false, error: 'Email and password are required.' });
  }

  // Pre-configured admin support
  if (email === 'admin@superfrist.vpn' && password === 'Admin@2026!') {
    const token = jwt.sign(
      { email, role: 'SUPER_ADMIN' },
      JWT_SECRET,
      { expiresIn: '24h' }
    );
    return res.json({
      success: true,
      data: {
        token,
        email,
        role: 'SUPER_ADMIN'
      }
    });
  }

  try {
    const result = await query('SELECT * FROM admin_users WHERE email = $1', [email]);
    if (result.rows.length === 0) {
      return res.status(401).json({ success: false, error: 'Invalid admin credentials.' });
    }

    const admin = result.rows[0];
    const match = await bcrypt.compare(password, admin.password_hash);
    if (!match) {
      return res.status(401).json({ success: false, error: 'Invalid admin credentials.' });
    }

    const token = jwt.sign(
      { id: admin.id, email: admin.email, role: admin.role },
      JWT_SECRET,
      { expiresIn: '24h' }
    );

    return res.json({
      success: true,
      data: {
        token,
        email: admin.email,
        role: admin.role
      }
    });
  } catch (e: any) {
    return res.status(500).json({ success: false, error: 'Admin authentication error.' });
  }
});

// 2. Metrics & Stats
router.get('/stats', requireAdmin, async (req: Request, res: Response) => {
  try {
    const totalCodesRes = await query('SELECT COUNT(*) FROM activation_codes');
    const unusedCodesRes = await query("SELECT COUNT(*) FROM activation_codes WHERE status = 'ACTIVE'");
    const usedCodesRes = await query("SELECT COUNT(*) FROM activation_codes WHERE status = 'USED'");
    const expiredCodesRes = await query("SELECT COUNT(*) FROM activation_codes WHERE status = 'EXPIRED' OR (expiry_date IS NOT NULL AND expiry_date <= NOW())");
    const activeConnsRes = await query("SELECT COUNT(*) FROM active_connections");
    const onlineServersRes = await query("SELECT COUNT(*) FROM vpn_servers WHERE status = 'ONLINE'");
    const offlineServersRes = await query("SELECT COUNT(*) FROM vpn_servers WHERE status != 'ONLINE'");

    res.json({
      success: true,
      data: {
        totalUsers: parseInt(usedCodesRes.rows[0].count),
        activeUsers: parseInt(usedCodesRes.rows[0].count),
        expiredUsers: parseInt(expiredCodesRes.rows[0].count),
        totalCodes: parseInt(totalCodesRes.rows[0].count),
        unusedCodes: parseInt(unusedCodesRes.rows[0].count),
        usedCodes: parseInt(usedCodesRes.rows[0].count),
        activeVpnConnections: parseInt(activeConnsRes.rows[0].count),
        onlineServers: parseInt(onlineServersRes.rows[0].count),
        offlineServers: parseInt(offlineServersRes.rows[0].count)
      }
    });
  } catch (e: any) {
    res.status(500).json({ success: false, error: 'Failed to retrieve stats.' });
  }
});

// 3. Generate Activation Codes
router.post('/codes/generate', requireAdmin, async (req: Request, res: Response) => {
  const { count = 5, validityDays = 30, prefix = 'VPN', notes = '' } = req.body;
  const num = Math.min(Math.max(parseInt(count) || 1, 1), 500);
  const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';

  const generatedCodes: any[] = [];

  try {
    for (let i = 0; i < num; i++) {
      const p1 = Array.from({ length: 4 }, () => chars[Math.floor(Math.random() * chars.length)]).join('');
      const p2 = Array.from({ length: 4 }, () => chars[Math.floor(Math.random() * chars.length)]).join('');
      const p3 = Array.from({ length: 4 }, () => chars[Math.floor(Math.random() * chars.length)]).join('');
      const code = `${prefix}-${p1}-${p2}-${p3}`;

      await query(
        `INSERT INTO activation_codes (code, status, validity_days, notes) VALUES ($1, 'ACTIVE', $2, $3)`,
        [code, validityDays, notes || `Admin Generated (${validityDays} Days)`]
      );

      generatedCodes.push({
        code,
        status: 'ACTIVE',
        validityDays,
        createdAt: new Date().toISOString()
      });
    }

    await query(
      `INSERT INTO audit_logs (action, target, details) VALUES ($1, $2, $3)`,
      ['BATCH_GENERATE', `${num} codes`, `Generated ${num} codes with ${validityDays} days validity.`]
    );

    res.json({
      success: true,
      message: `Successfully generated ${num} activation codes.`,
      data: generatedCodes
    });
  } catch (e: any) {
    res.status(500).json({ success: false, error: 'Failed to generate codes.' });
  }
});

// 4. List Codes
router.get('/codes', requireAdmin, async (req: Request, res: Response) => {
  const { status, search } = req.query;

  try {
    let sql = 'SELECT * FROM activation_codes WHERE 1=1';
    const params: any[] = [];

    if (status) {
      params.push(status);
      sql += ` AND status = $${params.length}`;
    }

    if (search) {
      params.push(`%${search}%`);
      sql += ` AND (code ILIKE $${params.length} OR device_id ILIKE $${params.length})`;
    }

    sql += ' ORDER BY created_at DESC LIMIT 200';

    const result = await query(sql, params);
    res.json({
      success: true,
      data: result.rows
    });
  } catch (e) {
    res.status(500).json({ success: false, error: 'Failed to fetch codes.' });
  }
});

// 5. Extend / Modify Code
router.put('/codes/:code', requireAdmin, async (req: Request, res: Response) => {
  const { code } = req.params;
  const { additionalDays, status, notes } = req.body;

  try {
    const existing = await query('SELECT * FROM activation_codes WHERE code = $1', [code]);
    if (existing.rows.length === 0) {
      return res.status(404).json({ success: false, error: 'Code not found.' });
    }

    const item = existing.rows[0];
    let newExpiry = item.expiry_date;

    if (additionalDays) {
      const base = item.expiry_date && new Date(item.expiry_date).getTime() > Date.now()
        ? new Date(item.expiry_date).getTime()
        : Date.now();
      newExpiry = new Date(base + (parseInt(additionalDays) * 86400000));
    }

    const updatedStatus = status || item.status;
    const updatedNotes = notes !== undefined ? notes : item.notes;

    await query(
      `UPDATE activation_codes SET expiry_date = $1, status = $2, notes = $3 WHERE code = $4`,
      [newExpiry, updatedStatus, updatedNotes, code]
    );

    res.json({
      success: true,
      message: 'Code updated successfully.'
    });
  } catch (e) {
    res.status(500).json({ success: false, error: 'Failed to update code.' });
  }
});

// 6. Reset Device Binding
router.post('/codes/:code/reset-device', requireAdmin, async (req: Request, res: Response) => {
  const { code } = req.params;

  try {
    await query('UPDATE activation_codes SET device_id = NULL WHERE code = $1', [code]);
    await query(
      `INSERT INTO audit_logs (action, target, details) VALUES ($1, $2, $3)`,
      ['RESET_DEVICE', code, 'Cleared device binding.']
    );

    res.json({
      success: true,
      message: `Device binding for ${code} reset successfully.`
    });
  } catch (e) {
    res.status(500).json({ success: false, error: 'Failed to reset device binding.' });
  }
});

// 7. Delete Code
router.delete('/codes/:code', requireAdmin, async (req: Request, res: Response) => {
  const { code } = req.params;

  try {
    await query('DELETE FROM activation_codes WHERE code = $1', [code]);
    res.json({ success: true, message: 'Code deleted.' });
  } catch (e) {
    res.status(500).json({ success: false, error: 'Failed to delete code.' });
  }
});

// 8. Live Connections
router.get('/connections', requireAdmin, async (req: Request, res: Response) => {
  try {
    const result = await query(`
      SELECT c.*, s.name as server_name, s.flag_emoji 
      FROM active_connections c
      LEFT JOIN vpn_servers s ON c.server_id = s.id
      ORDER BY c.connected_at DESC
    `);
    res.json({ success: true, data: result.rows });
  } catch (e) {
    res.status(500).json({ success: false, error: 'Failed to list connections.' });
  }
});

// 9. Terminate Connection
router.delete('/connections/:id', requireAdmin, async (req: Request, res: Response) => {
  const { id } = req.params;
  try {
    await query('DELETE FROM active_connections WHERE id = $1', [id]);
    res.json({ success: true, message: 'Connection terminated.' });
  } catch (e) {
    res.status(500).json({ success: false, error: 'Failed to terminate connection.' });
  }
});

// 10. Audit Logs
router.get('/audit-logs', requireAdmin, async (req: Request, res: Response) => {
  try {
    const result = await query('SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100');
    res.json({ success: true, data: result.rows });
  } catch (e) {
    res.status(500).json({ success: false, error: 'Failed to fetch audit logs.' });
  }
});

// 11. Export Codes to CSV
router.get('/export/codes', requireAdmin, async (req: Request, res: Response) => {
  try {
    const result = await query('SELECT code, status, validity_days, device_id, created_at, expiry_date FROM activation_codes ORDER BY created_at DESC');
    let csv = 'Code,Status,Validity Days,Device ID,Created At,Expiry Date\n';
    result.rows.forEach(r => {
      csv += `"${r.code}","${r.status}",${r.validity_days},"${r.device_id || ''}","${r.created_at}","${r.expiry_date || ''}"\n`;
    });

    res.setHeader('Content-Type', 'text/csv');
    res.setHeader('Content-Disposition', 'attachment; filename="super_frist_codes.csv"');
    res.send(csv);
  } catch (e) {
    res.status(500).send('Error exporting CSV.');
  }
});

export default router;
