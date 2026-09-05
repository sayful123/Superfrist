import express, { Request, Response, NextFunction } from 'express';
import cors from 'cors';
import helmet from 'helmet';
import rateLimit from 'express-rate-limit';
import path from 'path';
import activationRoutes from './routes/activation';
import vpnRoutes from './routes/vpn';
import adminRoutes from './routes/admin';

const app = express();
const PORT = process.env.PORT || 3000;

// Security Middlewares
app.use(helmet({
  contentSecurityPolicy: false // Allow inline scripts for dashboard
}));
app.use(cors());
app.use(express.json());

// Brute-force protection for activation and authentication endpoints
const authLimiter = rateLimit({
  windowMs: 15 * 60 * 1000, // 15 minutes
  max: 30, // 30 attempts per IP
  message: {
    success: false,
    error: 'Too many requests from this IP. Please try again later.'
  },
  standardHeaders: true,
  legacyHeaders: false,
});

app.use('/api/activation', authLimiter);
app.use('/api/auth', authLimiter);

// Health Check
app.get('/health', (req: Request, res: Response) => {
  res.json({
    status: 'HEALTHY',
    service: 'Super Frist VPN Core API',
    version: '1.0.0',
    timestamp: new Date().toISOString()
  });
});

// API Routes
app.use('/api/activation', activationRoutes);
app.use('/api/vpn', vpnRoutes);
app.use('/api/admin', adminRoutes);

// Serve Web Admin Panel statically
const adminPath = path.join(__dirname, '../../admin-panel');
app.use('/admin', express.static(adminPath));
app.get('/admin*', (req: Request, res: Response) => {
  res.sendFile(path.join(adminPath, 'index.html'));
});

// 404 Handler
app.use((req: Request, res: Response) => {
  res.status(404).json({
    success: false,
    error: `Endpoint ${req.method} ${req.originalUrl} not found.`
  });
});

// Global Error Handler
app.use((err: Error, req: Request, res: Response, next: NextFunction) => {
  console.error('[CRITICAL_ERROR]', err);
  res.status(500).json({
    success: false,
    error: 'Internal Server Error'
  });
});

app.listen(PORT, () => {
  console.log(`=========================================`);
  console.log(`🛡️  SUPER FRIST VPN BACKEND STARTED      `);
  console.log(`📡  Port: ${PORT}                        `);
  console.log(`🌐  API Gateway: http://localhost:${PORT}/api`);
  console.log(`🖥️  Admin Web Panel: http://localhost:${PORT}/admin`);
  console.log(`=========================================`);
});

export default app;
