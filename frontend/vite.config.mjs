import { defineConfig } from 'vite';
import path from 'path';

export default defineConfig({
  build: {
    outDir: '../src/main/resources/static/js',
    emptyOutDir: true,
    rollupOptions: {
      input: {
        'auth': path.resolve(__dirname, './src/auth.js'),
        'contact': path.resolve(__dirname, './src/contact.js'),
        'init-router': path.resolve(__dirname, './src/init-router.js'),
        'mega-menu': path.resolve(__dirname, './src/mega-menu.js'),
        'navigation-handler': path.resolve(__dirname, './src/navigation-handler.js'),
        'router': path.resolve(__dirname, './src/router.js'),
        'signin': path.resolve(__dirname, './src/signin.js'),
        'signup': path.resolve(__dirname, './src/signup.js'),
        'slideshow-video': path.resolve(__dirname, './src/slideshow-video.js'),
        'chat/chat': path.resolve(__dirname, './src/chat/chat.js'),
        'private/create-role': path.resolve(__dirname, './src/private/create-role.js'),
        'private/create-user': path.resolve(__dirname, './src/private/create-user.js'),
      },
      output: {
        entryFileNames: '[name].js'
      }
    }
  }
});
