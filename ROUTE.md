# Dokumentasi Route Artomoro Showroom

Dokumen ini menjelaskan semua route (endpoint HTTP) yang tersedia di aplikasi Artomoro Showroom, beserta parameter, method, dan template yang digunakan.

---

## 📋 Daftar Isi
1. [Auth & Home Routes](#auth--home-routes)
2. [Buyer Routes (Pembeli)](#buyer-routes-pembeli)
3. [Admin Dashboard Routes](#admin-dashboard-routes)
4. [Kendaraan Routes](#kendaraan-routes)
5. [Test Drive Routes](#test-drive-routes)
6. [Penjualan Routes](#penjualan-routes)
7. [Pembeli Routes](#pembeli-routes)
8. [Owner/Profil Routes](#ownerprofil-routes)
9. [Laporan Routes](#laporan-routes)

---

## Auth & Home Routes

### 1. **Halaman Login / Home**
- **Route:** `/`
- **Method:** `GET`
- **Controller:** `userController.java` (line 26)
- **Template:** `index.html`
- **Deskripsi:** Menampilkan halaman login dan registrasi
- **Parameters:** Tidak ada
- **Response:** Halaman HTML dengan form login dan registrasi

### 2. **Proses Login**
- **Route:** `/loginForm`
- **Method:** `POST`
- **Controller:** `userController.java` (line 32)
- **Parameters:**
  - `username` (String) - Username pengguna
  - `password` (String) - Password pengguna
- **Deskripsi:** Memproses login berdasarkan username dan password
- **Logic:**
  - Validasi username + password dari tabel `user`
  - Jika role = `Owner` atau `Admin` → redirect ke `/admin/Dashboard`
  - Jika role lain (Pembeli) → redirect ke `/buyer/home`
  - Jika gagal → tampilkan pesan error di halaman `index.html`
- **Response:** 
  - Sukses: Redirect ke dashboard atau buyer home
  - Gagal: Kembali ke halaman login dengan pesan error

### 3. **Proses Registrasi**
- **Route:** `/registerForm`
- **Method:** `POST`
- **Controller:** `userController.java` (line 71)
- **Parameters:**
  - `nama` (String) - Nama lengkap pengguna
  - `username` (String) - Username (min 3 karakter, unik)
  - `password` (String) - Password (min 6 karakter)
  - `confirmPassword` (String) - Konfirmasi password
- **Deskripsi:** Membuat akun pengguna baru
- **Logic:**
  1. Validasi password cocok dengan confirm password
  2. Cek username belum digunakan
  3. Insert ke tabel `user` dengan role default `Pembeli`
  4. Insert ke tabel `pembeli` dengan id_user baru
- **Response:**
  - Sukses: Pesan "Akun berhasil dibuat! Silakan login."
  - Gagal: Tampilkan form registrasi dengan pesan error

### 4. **Live Check Username (AJAX)**
- **Route:** `/checkUsername`
- **Method:** `GET`
- **Controller:** `userController.java` (line 126)
- **Parameters:**
  - `username` (String) - Username yang akan dicek
- **Deskripsi:** Cek ketersediaan username secara real-time (saat user mengetik)
- **Response:** JSON `{ "taken": true/false }`

---

## Buyer Routes (Pembeli)

### 5. **Halaman Buyer Home / Katalog Kendaraan**
- **Route:** `/buyer/home`
- **Method:** `GET`
- **Controller:** `userController.java` (line 65)
- **Template:** `buyer-home.html`
- **Deskripsi:** Menampilkan daftar kendaraan yang tersedia untuk pembeli
- **Parameters:** Tidak ada
- **Data Model:**
  - `kendaraan` - List semua kendaraan dengan info status, harga, tahun
  - `nama` - Nama pembeli (untuk header)
- **Response:** Halaman HTML dengan katalog kendaraan, filter, dan link ke test drive

### 6. **Halaman Buyer Test Drive**
- **Route:** `/buyer/testdrive`
- **Method:** `GET`
- **Controller:** `testdriveController.java` (line 20)
- **Template:** `buyer-testdrive.html`
- **Parameters:**
  - `kendaraan` (Integer) - ID kendaraan (opsional, untuk pre-select dropdown)
- **Deskripsi:** Menampilkan halaman untuk membuat jadwal test drive
- **Data Model:**
  - `kendaraanList` - List kendaraan tersedia untuk dropdown
  - `myTestdrives` - List jadwal test drive pembeli saat ini
  - `kendaraanMap` - Map untuk lookup data kendaraan
  - `selectedKendaraan` - ID kendaraan yang di-select (jika ada)
- **Response:** Halaman HTML dengan form jadwal test drive dan history jadwal pembeli

### 7. **Submit Jadwal Test Drive (Buyer)**
- **Route:** `/buyer/testdrive`
- **Method:** `POST`
- **Controller:** `testdriveController.java` (dalam `/buyer/testdrive` GET)
- **Parameters:**
  - `idKendaraan` (Integer) - ID kendaraan yang dipilih
  - `tanggal` (String) - Tanggal test drive (format: YYYY-MM-DD)
  - `jam` (String) - Jam test drive (format: HH:mm)
  - `catatan` (String) - Catatan tambahan (opsional)
- **Deskripsi:** Menyimpan jadwal test drive baru ke database
- **Status Default:** `Pending`
- **Response:** Redirect ke `/buyer/testdrive` dengan flash message sukses/gagal

### 8. **Halaman Buyer Pesanan**
- **Route:** `/buyer/pesanan`
- **Method:** `GET`
- **Controller:** `pembeliController.java` (diimplementasikan melalui template)
- **Template:** `buyer-pesanan.html`
- **Deskripsi:** Menampilkan riwayat pesanan/penjualan pembeli
- **Status:** **Belum ada endpoint** - Template ada tapi controller belum terbuat

### 9. **Halaman Buyer Profil**
- **Route:** `/buyer/profil`
- **Method:** `GET`
- **Controller:** `ownerController.java` / user profile handler
- **Template:** `buyer-profil.html`
- **Deskripsi:** Menampilkan data profil pembeli
- **Status:** **Belum ada endpoint dedicated** - Perlu dibuat

---

## Admin Dashboard Routes

### 10. **Admin Dashboard**
- **Route:** `/admin/Dashboard`
- **Method:** `GET`
- **Controller:** `dashboardController.java` (line 21)
- **Template:** `admin/Dasboard.html`
- **Deskripsi:** Menampilkan dashboard admin dengan statistik penjualan, test drive, kendaraan, dll
- **Data Model:**
  - `listAktivitas` - 5 aktivitas terbaru (penjualan + test drive)
  - `grafikPenjualan` - Data grafik 6 bulan terakhir
  - `totalPendapatan6Bulan` - Total pendapatan 6 bulan terakhir
  - `persentasePertumbuhan` - Persentase pertumbuhan vs periode lalu
  - `totalKendaraan`, `penjualanBulanIni`, `testDriveAktif`, `permintaanPending` - Statistik utama
  - `kendaraanUnggulan` - 6 kendaraan terbaru
  - `listTransaksi` - 5 transaksi penjualan terbaru
  - Dan banyak data statistik lainnya
- **Response:** Halaman HTML dashboard dengan charts dan statistik

---

## Kendaraan Routes

### 11. **Halaman Manajemen Kendaraan (Admin)**
- **Route:** `/kendaraan`
- **Method:** `GET`
- **Controller:** `kendaraanController.java` (line 35)
- **Template:** `admin/kendaraan.html`
- **Deskripsi:** Menampilkan daftar semua kendaraan dengan form tambah/edit
- **Data Model:**
  - List kendaraan dari database dengan join ke tabel `mobil` dan `motor`
  - Dropdown pembeli
  - Statistik stok kendaraan
- **Response:** Halaman HTML dengan tabel kendaraan dan form

### 12. **Tambah Kendaraan (Admin)**
- **Route:** `/kendaraan/tambah`
- **Method:** `POST`
- **Controller:** `kendaraanController.java` (line 71)
- **Parameters:**
  - `merk` (String) - Merek kendaraan
  - `model` (String) - Model kendaraan
  - `tahun` (Integer) - Tahun produksi
  - `harga` (Decimal) - Harga kendaraan
  - `status` (String) - Status (Tersedia, Booking, Terjual)
  - `jenis` (String) - Jenis (Mobil atau Motor)
  - Dan parameter spesifik jenis kendaraan (mesin, kapasitas, dll)
- **Deskripsi:** Menambah data kendaraan baru
- **Response:** Redirect ke `/kendaraan`

### 13. **Hapus Kendaraan (Admin)**
- **Route:** `/kendaraan/hapus/{id}`
- **Method:** `GET`
- **Controller:** `kendaraanController.java` (line 131)
- **Parameters:**
  - `id` (Integer, Path Variable) - ID kendaraan yang akan dihapus
- **Deskripsi:** Menghapus data kendaraan dari database
- **Response:** Redirect ke `/kendaraan`

---

## Test Drive Routes

### 14. **Halaman Manajemen Test Drive (Admin)**
- **Route:** `/testdrive`
- **Method:** `GET`
- **Controller:** `testdriveController.java` (direncanakan untuk admin)
- **Template:** `admin/testdrive.html`
- **Deskripsi:** Menampilkan daftar semua jadwal test drive (admin view)
- **Status:** **Route tidak aktif** - Hanya `/buyer/testdrive` yang tersedia
- **Future:** Perlu menambahkan GET `/testdrive` untuk admin

### 15. **Tambah Jadwal Test Drive (Admin)**
- **Route:** `/testdrive/tambah`
- **Method:** `POST`
- **Controller:** `testdriveController.java` (line 80)
- **Parameters:**
  - `idPembeli` (Integer) - ID pembeli
  - `idKendaraan` (Integer) - ID kendaraan
  - `tanggal` (String) - Tanggal test drive
  - `jam` (String) - Jam test drive
  - `status` (String) - Status (Pending, Aktif, Selesai, Ditolak)
  - `catatan` (String) - Catatan (opsional)
- **Deskripsi:** Admin menambah jadwal test drive untuk pembeli
- **Response:** Redirect ke `/testdrive`

### 16. **Update Status Test Drive (Admin)**
- **Route:** `/testdrive/updateStatus`
- **Method:** `POST`
- **Controller:** `testdriveController.java` (line 105)
- **Parameters:**
  - `idTestdrive` (Integer) - ID test drive
  - `status` (String) - Status baru (Aktif, Selesai, Ditolak, Dibatalkan)
- **Deskripsi:** Admin mengubah status jadwal test drive
- **Response:** Redirect ke `/testdrive`

---

## Penjualan Routes

### 17. **Halaman Manajemen Penjualan (Admin)**
- **Route:** `/penjualan`
- **Method:** `GET`
- **Controller:** `penjualanController.java` (line 20)
- **Template:** `admin/penjualan.html`
- **Deskripsi:** Menampilkan daftar semua transaksi penjualan
- **Data Model:**
  - List penjualan dengan join ke pembeli, kendaraan, owner
  - Statistik penjualan
- **Response:** Halaman HTML dengan tabel penjualan dan form

### 18. **Tambah Penjualan (Admin)**
- **Route:** `/penjualan/tambah`
- **Method:** `POST`
- **Controller:** `penjualanController.java` (line 101)
- **Parameters:**
  - `idPembeli` (Integer) - ID pembeli
  - `idKendaraan` (Integer) - ID kendaraan
  - `tanggal` (String) - Tanggal penjualan
  - `status` (String) - Status penjualan (Pending, Selesai, Ditolak, Dibatalkan)
- **Deskripsi:** Admin membuat record penjualan baru
- **Response:** Redirect ke `/penjualan`

### 19. **Update Status Penjualan (Admin)**
- **Route:** `/penjualan/updateStatus`
- **Method:** `POST`
- **Controller:** `penjualanController.java` (line 121)
- **Parameters:**
  - `idPenjualan` (Integer) - ID penjualan
  - `status` (String) - Status baru (Pending, Selesai, Ditolak, Dibatalkan)
- **Deskripsi:** Admin mengubah status transaksi penjualan
- **Response:** Redirect ke `/penjualan`

---

## Pembeli Routes

### 20. **Halaman Manajemen Pembeli (Admin)**
- **Route:** `/pembeli`
- **Method:** `GET`
- **Controller:** `pembeliController.java` (line 23)
- **Template:** `admin/pembeli.html`
- **Deskripsi:** Menampilkan daftar semua data pembeli
- **Data Model:**
  - List pembeli dengan join ke tabel user
  - Informasi kontak, riwayat pesanan, test drive
- **Response:** Halaman HTML dengan tabel pembeli

### 21. **Tambah Pembeli (Admin)**
- **Route:** `/pembeli/tambah`
- **Method:** `POST`
- **Controller:** `pembeliController.java` (line 73)
- **Parameters:**
  - `nama` (String) - Nama pembeli
  - `kontak` (String) - Nomor kontak pembeli
- **Deskripsi:** Admin menambahkan data pembeli baru
- **Response:** Redirect ke `/pembeli`

### 22. **Detail Pembeli (Admin)**
- **Route:** `/pembeli/detail/{id}`
- **Method:** `GET`
- **Controller:** `pembeliController.java` (line 108)
- **Parameters:**
  - `id` (Integer, Path Variable) - ID pembeli
- **Template:** Detail pembeli view (embedded atau modal)
- **Deskripsi:** Menampilkan detail lengkap pembeli tertentu
- **Response:** Data detail pembeli (JSON atau HTML)

---

## Owner/Profil Routes

### 23. **Halaman Owner Profil**
- **Route:** `/profil`
- **Method:** `GET`
- **Controller:** `ownerController.java` (line 20)
- **Template:** `admin/profil.html`
- **Deskripsi:** Menampilkan profil owner/admin
- **Data Model:**
  - Data profil owner (nama, email, kontak, kota, provinsi)
  - Form edit profil
- **Response:** Halaman HTML dengan info profil dan form edit

### 24. **Update Profil Owner**
- **Route:** `/profil/update`
- **Method:** `POST`
- **Controller:** `ownerController.java` (line 70)
- **Parameters:**
  - `nama` (String) - Nama owner
  - `email` (String) - Email
  - `kontak` (String) - Nomor kontak
  - `kota` (String) - Kota
  - `provinsi` (String) - Provinsi
- **Deskripsi:** Update data profil owner
- **Response:** Redirect ke `/profil` dengan pesan sukses/gagal

### 25. **Update Password Owner**
- **Route:** `/profil/password`
- **Method:** `POST`
- **Controller:** `ownerController.java` (line 91)
- **Parameters:**
  - `passwordLama` (String) - Password lama (untuk verifikasi)
  - `passwordBaru` (String) - Password baru
  - `confirmPassword` (String) - Konfirmasi password baru
- **Deskripsi:** Update password owner
- **Response:** Redirect ke `/profil` dengan pesan sukses/gagal

---

## Laporan Routes

### 26. **Halaman Laporan (Admin)**
- **Route:** `/laporan`
- **Method:** `GET`
- **Controller:** `laporanController.java` (line 20)
- **Template:** `admin/laporan.html`
- **Deskripsi:** Menampilkan laporan penjualan dan statistik bisnis
- **Data Model:**
  - Laporan penjualan per bulan
  - Laporan test drive
  - Laporan pembeli
  - Export laporan (PDF/Excel - jika diimplementasikan)
- **Response:** Halaman HTML dengan laporan dan grafik

---

## 🔐 Alur Autentikasi & Otorisasi

```
User akses "/" 
    ↓
Lihat halaman login (index.html)
    ↓
Submit form login ke "/loginForm"
    ↓
Sistem cek database tabel "user"
    ↓
┌─────────────────────────────────────┐
│ Jika role = Owner/Admin             │
│ → Redirect ke /admin/Dashboard      │
│   (dashboard admin)                 │
└─────────────────────────────────────┘
         ↓
┌─────────────────────────────────────┐
│ Jika role = Pembeli                 │
│ → Redirect ke /buyer/home           │
│   (katalog kendaraan buyer)         │
└─────────────────────────────────────┘
         ↓
Jika login gagal
→ Tampil pesan error di "/", ulangi
```

---

## 📊 Struktur Database (Tabel)

### Tabel `user`
```
id_user (PK)
nama
username (UNIQUE)
email
password
role (Owner, Admin, Pembeli)
```

### Tabel `pembeli`
```
id_pembeli (PK)
id_user (FK)
kontak
```

### Tabel `owner`
```
id_owner (PK)
id_user (FK)
kontak
kota
provinsi
```

### Tabel `kendaraan`
```
id_kendaraan (PK)
merk
model
tahun
harga
status (Tersedia, Booking, Terjual)
foto
```

### Tabel `mobil`
```
id_mobil (PK)
id_kendaraan (FK)
mesin_mobil
jenis_mobil
transmisi_mobil
kapasitas_mobil
```

### Tabel `motor`
```
id_motor (PK)
id_kendaraan (FK)
cc
jenis_motor
kapasitas_tangki
```

### Tabel `testdrive`
```
id_testdrive (PK)
tanggal
jam
status (Pending, Aktif, Selesai, Ditolak, Dibatalkan)
catatan
id_pembeli (FK)
id_kendaraan (FK)
id_owner (FK)
```

### Tabel `penjualan`
```
id_penjualan (PK)
tanggal
status (Pending, Selesai, Ditolak, Dibatalkan)
id_pembeli (FK)
id_kendaraan (FK)
id_owner (FK)
```

---

## ⚠️ Routes yang Belum Diimplementasikan

1. **Logout** (`/logout`) - Perlu ditambahkan untuk session management
2. **Admin Test Drive View** (`/testdrive` GET) - Hanya ada buyer view
3. **Buyer Pesanan** (`/buyer/pesanan` GET) - Template ada tapi controller belum
4. **Buyer Profil** (`/buyer/profil` GET) - Template ada tapi controller belum
5. **Laporan Export** - Jika ingin export PDF/Excel

---

## 🔧 Catatan Teknis

- **Template Engine:** Thymeleaf
- **Port Default:** 8080 (dapat diubah dengan `--server.port=XXXX`)
- **Database:** MariaDB/MySQL (konfigurasi di `application.properties`)
- **Authentication:** Simple (username + password, belum menggunakan Spring Security)
- **Session:** Belum ada session management, perlu ditambahkan untuk production

---

## 📝 Contoh Penggunaan

### Login sebagai Owner
```
GET / → Lihat halaman login
POST /loginForm → username: adminowner, password: 12345, role: Owner
↓
Redirect ke /admin/Dashboard → Tampil dashboard admin
```

### Login sebagai Pembeli
```
GET / → Lihat halaman login
POST /loginForm → username: jopanbekti, password: 1234567890, role: Pembeli
↓
Redirect ke /buyer/home → Tampil katalog kendaraan
```

### Membuat Jadwal Test Drive (Pembeli)
```
GET /buyer/home → Lihat katalog, klik tombol "Jadwalkan Test Drive"
↓
GET /buyer/testdrive → Tampil form jadwal test drive
↓
POST /buyer/testdrive → Isi form, klik submit
↓
Redirect ke /buyer/testdrive → Tampil pesan sukses, jadwal muncul di history
```

---

**Terakhir diperbarui:** 7 Juni 2026
**Versi Aplikasi:** 0.0.1-SNAPSHOT
