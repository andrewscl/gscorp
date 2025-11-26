import { defineConfig } from 'vite';

export default defineConfig({
  base: '/',
  build: {
    outDir: '../src/main/resources/static/assets',     // Dónde va el output de Vite (ajustado a Spring Boot)
    emptyOutDir: true,
    rollupOptions: {
      input: {
        'private/client': 'src/private/dashboard/client-dashboard.ts',  // Entrada Client Dashboard
        // Puedes agregar más entradas aquí
      },
      output: {
        entryFileNames: '[name].js',                    // Resultado: private/dashboard.js, private/client.js
        chunkFileNames: 'chunks/[name]-[hash].js',
        assetFileNames: 'assets/[name]-[hash][extname]',
      }
    }
  },
  server: {
    port: 5173,
    proxy: { '/api': 'http://localhost:8080' }          // API backend proxy para desarrollo
  }
});