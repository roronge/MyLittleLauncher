# My Little Launcher — v0.3.0 (Revamped Edition)

Custom Android Home Launcher berbasis **Kotlin + Jetpack Compose** dengan arsitektur modular, efek *glassmorphism*, pelacak milestone anak, dan optimasi untuk POCO F3 / HyperOS.

## Pembaruan Utama v0.3.0
- **Arsitektur Modular**: Terpisah menjadi `model/`, `data/`, `ui/theme/`, `ui/components/`, dan `ui/screens/`.
- **2-Page Home Workspace**:
  - *Halaman 1*: Tampilan bersih yang menonjolkan foto keluarga, jam/tanggal interaktif (ketuk jam untuk Alarm, ketuk tanggal untuk Kalender), badge sapaan & baterai, serta kartu usia milestone otomatis.
  - *Halaman 2*: Bilah pencarian cepat dan grid **Aplikasi Disematkan (Pinned Apps)** hingga 8 aplikasi.
- **Gestur Navigasi Mulus**:
  - Geser ke atas (*Swipe Up*) di Home untuk membuka App Library.
  - Geser ke bawah (*Swipe Down*) di Home untuk langsung mencari aplikasi (*Quick Search*).
  - Mendukung tombol *Back* sistem dan tombol *Home* (`onNewIntent`) untuk kembali ke beranda utama.
- **App Library Pintar**:
  - Filter kategori otomatis (*Semua, Favorit, Komunikasi, Hiburan & Foto, Alat & Utilitas, Lainnya*).
  - *Fast Alphabet Scrubber* (A–Z) di sisi kanan layar.
  - Tekan lama ikon aplikasi untuk menu cepat: *Favorit, Sematkan di Home, Sembunyikan Aplikasi, Info Aplikasi*.
  - Sinkronisasi otomatis (*BroadcastReceiver*) saat aplikasi baru diinstal atau dihapus.
- **Personalisasi Lengkap**:
  - 4 Preset Tema (*Soft Mint Luxury, Warm Peach Blossom, Lavender Lullaby, Midnight Forest AMOLED*).
  - Pilihan bentuk ikon (*Squircle, Circle, Rounded*) & kepadatan grid (3–5 kolom).
  - Pemilihan 4 aplikasi khusus untuk Floating Dock.
  - Kustomisasi nama anak, tanggal lahir (menghitung usia bulan/hari secara otomatis), dan kutipan pribadi.

## Build via GitHub Actions atau Lokal
```bash
gradle --no-daemon :app:assembleDebug
```
Output APK berada di `app/build/outputs/apk/debug/app-debug.apk`.
