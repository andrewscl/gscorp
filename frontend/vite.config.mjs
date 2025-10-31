import { defineConfig } from 'vite';
import path from 'path';

export default defineConfig({
  build: {
    outDir: '../src/main/resources/static/js',
    emptyOutDir: true,
    rollupOptions: {
      input: {
        'auth': path.resolve(__dirname, './src/auth.js'),
        'auth/define-password': path.resolve(__dirname, './src/auth/define-password.js'),
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
        'private/admin/users/create-user': path.resolve(__dirname, './src/private/admin/users/create-user.js'),
        'private/admin/users/edit-user': path.resolve(__dirname, './src/private/admin/users/edit-user.js'),
        'private/admin/users/invite-user': path.resolve(__dirname, './src/private/admin/users/invite-user.js'),
        'private/attendance/attendance': path.resolve(__dirname, './src/private/attendance/attendance.js'),
        'private/attendance/attendance-table': path.resolve(__dirname, './src/private/attendance/attendance-table.js'),
        'private/menu/private-menu': path.resolve(__dirname, './src/private/menu/private-menu.js'),
        'private/clients/create-client': path.resolve(__dirname, './src/private/clients/create-client.js'),
        'private/clients/edit-client': path.resolve(__dirname, './src/private/clients/edit-client.js'),
        'private/sites/create-site': path.resolve(__dirname, './src/private/sites/create-site.js'),
        'private/sites/view-site': path.resolve(__dirname, './src/private/sites/view-site.js'),
        'private/sites/edit-site': path.resolve(__dirname, './src/private/sites/edit-site.js'),
        'private/projects/create-project': path.resolve(__dirname, './src/private/projects/create-project.js'),
        'private/projects/edit-project': path.resolve(__dirname, './src/private/projects/edit-project.js'),
        'private/projects/view-project': path.resolve(__dirname, './src/private/projects/view-project.js'),
        'private/employees/create-employee': path.resolve(__dirname, './src/private/employees/create-employee.js'),
        'private/banks/create-bank': path.resolve(__dirname, './src/private/banks/create-bank.js'),
        'private/positions/create-position': path.resolve(__dirname, './src/private/positions/create-position.js'),
        'private/positions/edit-position': path.resolve(__dirname, './src/private/positions/edit-position.js'),
        'private/shift-patterns/edit-shift-pattern': path.resolve(__dirname, './src/private/shift-patterns/edit-shift-pattern.js'),
        'private/shift-patterns/create-shift-pattern': path.resolve(__dirname, './src/private/shift-patterns/create-shift-pattern.js'),
        'private/professions/create-profession': path.resolve(__dirname, './src/private/professions/create-profession.js'),
        'private/nationalities/edit-nationality': path.resolve(__dirname, './src/private/nationalities/edit-nationality.js'),
        'private/nationalities/create-nationality': path.resolve(__dirname, './src/private/nationalities/create-nationality.js'),
        'private/shift-requests/edit-shift-request': path.resolve(__dirname, './src/private/shift-requests/edit-shift-request.js'),
        'private/shift-requests/create-shift-request': path.resolve(__dirname, './src/private/shift-requests/create-shift-request.js'),
        'private/shifts/create-shift': path.resolve(__dirname, './src/private/shifts/create-shift.js'),
        'private/shifts/create-shifts-bulk': path.resolve(__dirname, './src/private/shifts/create-shifts-bulk.js'),
        'private/sites/set-coordinates': path.resolve(__dirname, './src/private/sites/set-coordinates.js'),
        'private/site-supervision-visits/view-site-supervision-visit': path.resolve(__dirname, './src/private/site-supervision-visits/view-site-supervision-visit.js'),
        'private/site-supervision-visits/create-site-supervision-visit': path.resolve(__dirname, './src/private/site-supervision-visits/create-site-supervision-visit.js')

      },
      output: {
        entryFileNames: '[name].js'
      }
    }
  }
});
