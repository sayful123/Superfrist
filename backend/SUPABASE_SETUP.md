# 🚀 Super Frist VPN - Supabase Connection Guide

এই নির্দেশিকাটি অনুসরণ করে আপনি খুব সহজে **Supabase** এর সাথে **Super Frist VPN** ব্যাকএন্ড সম্পূর্ণ ফ্রিতে কানেক্ট করতে পারবেন।

---

## ধাপ ১: Supabase অ্যাকাউন্ট তৈরি ও প্রজেক্ট সেটআপ

1. ব্রাউজারে যান: [https://supabase.com](https://supabase.com)
2. **Start your project** এ ক্লিক করে একটি ফ্রি অ্যাকাউন্ট খুলুন (GitHub বা Email দিয়ে)।
3. ড্যাশবোর্ডে **New Project** এ ক্লিক করুন:
   - **Name**: `superfrist-vpn` (অথবা আপনার পছন্দের নাম)
   - **Database Password**: একটি শক্তিশালী পাসওয়ার্ড দিন এবং এটি সংরক্ষণ করে রাখুন (যেমন: `SuperFristPass2026!`)
   - **Region**: আপনার কাছাকাছি কোনো রিজিয়ন বেছে নিন (যেমন: `Singapore` বা `Frankfurt`)
   - **Pricing Plan**: `Free` সিলেক্ট করুন।
4. **Create new project** এ ক্লিক করে ১-২ মিনিট অপেক্ষা করুন প্রজেক্টটি প্রস্তুত হওয়ার জন্য।

---

## ধাপ ২: স্কিমা (Schema) ও টেবিল তৈরি করা

1. Supabase ড্যাশবোর্ডের বাম পাশের মেনু থেকে **SQL Editor** এ ক্লিক করুন।
2. **New query** তে ক্লিক করুন।
3. প্রজেক্টের `backend/schema.sql` ফাইলের সমস্ত কোড কপি করে SQL এডিটরে পেস্ট করুন।
4. নিচে থাকা সবুজ **Run** বাটনে ক্লিক করুন।
5. এতে সাথে সাথে তৈরি হয়ে যাবে:
   - `admin_users` (এডমিন অ্যাকাউন্ট)
   - `activation_codes` (ভিপিএন অ্যাক্টিভেশন কোড)
   - `vpn_servers` (গ্লোবাল ভিপিএন সার্ভার নোড)
   - `active_connections` (লাইভ টানেল ও টেলিমেট্রি)
   - `audit_logs` (নিরাপত্তা ও অ্যাক্টিভিটি লগ)
   - এবং প্রাথমিক টেস্ট কোডগুলো সিড হয়ে যাবে!

---

## ধাপ ৩: ডাটাবেইজ কানেকশন স্ট্রিং সংগ্রহ করা

1. Supabase ড্যাশবোর্ডের বাম পাশের নিচে **Project Settings** (গিয়ার আইকন ⚙️) এ যান।
2. মেনু থেকে **Database** এ ক্লিক করুন।
3. নিচে স্ক্রল করে **Connection string** সেকশনে যান।
4. **URI** ট্যাবে ক্লিক করুন।
5. আপনি এরকম একটি স্ট্রিং দেখতে পাবেন:
   ```text
   postgresql://postgres:[YOUR-PASSWORD]@db.xxxxxxx.supabase.co:5432/postgres
   ```
6. এটি কপি করুন এবং `[YOUR-PASSWORD]` এর জায়গায় আপনার প্রজেক্ট তৈরির সময় দেয়া আসল ডাটাবেস পাসওয়ার্ডটি বসিয়ে দিন।

---

## ধাপ ৪: ব্যাকএন্ডে কনফিগারেশন সেট করা

আপনার `backend` ফোল্ডারে একটি `.env` ফাইল তৈরি করুন (বা `.env.example` রিনেম করুন) এবং নিচের মতো বসান:

```env
PORT=3000
NODE_ENV=production
JWT_SECRET=super_frist_vpn_production_secret_key_2026

# Supabase Connection
DATABASE_URL=postgresql://postgres:আপনার_পাসওয়ার্ড@db.আপনার_প্রজেক্ট_আইডি.supabase.co:5432/postgres
DB_SSL=true
```

---

## ধাপ ৫: সার্ভার চালু ও টেস্ট করা

১. ডিপেন্ডেন্সি ইনস্টল ও বিল্ড করুন:
```bash
cd backend
npm install
npm run build
npm start
```

২. সার্ভার চালু হলে আপনি দেখতে পাবেন:
```text
🛡️  SUPER FRIST VPN BACKEND STARTED
📡  Port: 3000
🌐  API Gateway: http://localhost:3000/api
🖥️  Admin Web Panel: http://localhost:3000/admin
```

৩. ব্রাউজারে `http://localhost:3000/admin` ওপেন করুন:
   - **Email:** `admin@superfrist.vpn`
   - **Password:** `Admin@2026!`

এখন আপনার মোবাইল অ্যাপ এবং ওয়েব এডমিন প্যানেল সরাসরি ক্লাউড **Supabase** ডাটাবেজের সাথে লাইভ কানেক্টেড থাকবে!
