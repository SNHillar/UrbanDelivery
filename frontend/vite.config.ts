import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    // En desarrollo el front llama a /api y Vite lo reenvía al backend: no hace falta CORS
    proxy: { '/api': 'http://localhost:8080' },
  },
})
