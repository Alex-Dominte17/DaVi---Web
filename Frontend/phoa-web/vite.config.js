// import { defineConfig } from 'vite'
// import react from '@vitejs/plugin-react'

// // https://vite.dev/config/
// // export default defineConfig({
// //   plugins: [react()],
// // })


// export default defineConfig({
//   server: {
//     proxy: {
//       '/api': {
//         target: 'http://localhost:8080', // Adresa serverului tău Spring Boot
//         changeOrigin: true,
//         secure: false,
//       }
//     }
//   }
// })


import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080', // Adresa unde rulează Spring Boot
        changeOrigin: true,
        secure: false,
        // Această linie asigură că /api rămâne în URL când ajunge la Spring
        rewrite: (path) => path 
      }
    }
  }
})
