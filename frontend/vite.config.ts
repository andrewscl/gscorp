import { defineConfig } from 'vite';

export default defineConfig({
  base: '/',
  build: {
    outDir: '../src/main/resources/static/assets',
    emptyOutDir: true,
    rollupOptions: {
      input: {
        'private/dashboard': 'src/private/dashboard.ts',
        'private/client': 'src/private/client/client-dashboard.ts',
         // entrada nombrada
      },
      output: {
        entryFileNames: '[name].js',                    // nombre estable
        chunkFileNames: 'chunks/[name]-[hash].js',
        assetFileNames: 'assets/[name]-[hash][extname]',
      }
    }
  },
  server: {
    port: 5173,
    proxy: { '/api': 'http://localhost:8080' }
  }
});
