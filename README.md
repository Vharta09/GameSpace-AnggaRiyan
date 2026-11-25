# Game Space (Poco X7 Pro, Android 13-17)

Buka folder ini di Android Studio (Ladybug atau lebih baru), Sync, lalu Run di perangkat.
minSdk 33 (Android 13). Berjalan di Android 14-17 karena hanya memakai API publik.

## Langkah pertama di HP
1. Buka app -> "Beri izin yang dibutuhkan": Akses penggunaan + Tampil di atas aplikasi (wajib).
2. Opsional: Jangan ganggu, Ubah pengaturan sistem, Aksesibilitas (screenshot/rekam).
3. HyperOS: Info app -> Penghemat baterai -> Tanpa batasan, dan aktifkan Mulai otomatis.
4. Aktifkan Game Space, lalu buka game. Panel muncul di tepi kiri layar.

## Mode performa sistem (opsional, lewat ADB)
adb shell cmd game mode performance <package.game>
