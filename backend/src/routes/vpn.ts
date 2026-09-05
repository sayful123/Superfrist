import { Router, Request, Response } from 'express';
import jwt from 'jsonwebtoken';
import { query } from '../database';

const router = Router();
const JWT_SECRET = process.env.JWT_SECRET || 'super_frist_vpn_production_secret_key_2026';

// Middleware for token validation
const authenticateUser = (req: Request, res: Response, next: Function) => {
  const authHeader = req.headers.authorization;
  if (!authHeader) {
    return res.status(401).json({ success: false, error: 'Unauthorized: Missing token.' });
  }

  const token = authHeader.replace('Bearer ', '');
  try {
    const decoded: any = jwt.verify(token, JWT_SECRET);
    (req as any).user = decoded;
    next();
  } catch (e) {
    return res.status(401).json({ success: false, error: 'Unauthorized: Invalid token.' });
  }
};

// 1. Get VPN Servers
router.get('/servers', async (req: Request, res: Response) => {
  try {
    const result = await query(
      'SELECT id, name, country, country_code, flag_emoji, city, host, port, protocol, public_key, ping_ms, load_percentage, status, is_visible FROM vpn_servers WHERE is_visible = true ORDER BY ping_ms ASC'
    );
    res.json({
      success: true,
      data: result.rows
    });
  } catch (error: any) {
    res.status(500).json({ success: false, error: 'Failed to fetch servers.' });
  }
});

// 2. Connect VPN Session
router.post('/connect', authenticateUser, async (req: Request, res: Response) => {
  const user = (req as any).user;
  const { serverId, protocol } = req.body;

  try {
    const srvRes = await query('SELECT * FROM vpn_servers WHERE id = $1', [serverId]);
    if (srvRes.rows.length === 0) {
      return res.status(404).json({ success: false, error: 'VPN server not found.' });
    }

    const server = srvRes.rows[0];

    // Remove any stale connections
    await query('DELETE FROM active_connections WHERE user_id = $1', [user.userId]);

    // Insert new active connection
    const connRes = await query(
      `INSERT INTO active_connections (user_id, code, device_id, server_id, client_ip, virtual_ip, protocol, status)
       VALUES ($1, $2, $3, $4, $5, $6, $7, 'CONNECTED') RETURNING id`,
      [user.userId, user.code, user.deviceId, serverId, req.ip || '127.0.0.1', '10.8.0.2', protocol || 'WIREGUARD']
    );

    // Update server active users count
    await query('UPDATE vpn_servers SET current_users = current_users + 1 WHERE id = $1', [serverId]);

    res.json({
      success: true,
      message: 'VPN Tunnel Established.',
      data: {
        connectionId: connRes.rows[0].id,
        serverName: server.name,
        virtualIp: '10.8.0.2',
        dns: ['1.1.1.1', '8.8.8.8'],
        mtu: 1420
      }
    });
  } catch (e: any) {
    res.status(500).json({ success: false, error: 'Failed to register connection.' });
  }
});

// 3. Disconnect VPN Session
router.post('/disconnect', authenticateUser, async (req: Request, res: Response) => {
  const user = (req as any).user;

  try {
    const conns = await query('SELECT server_id FROM active_connections WHERE user_id = $1', [user.userId]);
    if (conns.rows.length > 0) {
      const srvId = conns.rows[0].server_id;
      await query('UPDATE vpn_servers SET current_users = GREATEST(0, current_users - 1) WHERE id = $1', [srvId]);
    }

    await query('DELETE FROM active_connections WHERE user_id = $1', [user.userId]);

    res.json({
      success: true,
      message: 'VPN Tunnel closed cleanly.'
    });
  } catch (e: any) {
    res.status(500).json({ success: false, error: 'Failed to terminate connection.' });
  }
});

// 4. Get WireGuard Configuration
router.get('/config', authenticateUser, async (req: Request, res: Response) => {
  const { serverId } = req.query;

  try {
    const srvRes = await query('SELECT * FROM vpn_servers WHERE id = $1', [serverId]);
    if (srvRes.rows.length === 0) {
      return res.status(404).json({ success: false, error: 'VPN server not found.' });
    }

    const s = srvRes.rows[0];

    res.json({
      success: true,
      data: {
        interfaceIp: '10.8.0.2/24',
        dns: ['1.1.1.1', '8.8.8.8'],
        endpoint: `${s.host}:${s.port}`,
        serverPublicKey: s.public_key,
        allowedIps: ['0.0.0.0/0', '::/0'],
        mtu: 1420
      }
    });
  } catch (e) {
    res.status(500).json({ success: false, error: 'Failed to generate VPN config.' });
  }
});

export default router;
