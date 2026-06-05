import { defineConfig, transformWithOxc } from 'vite';
import react from '@vitejs/plugin-react';

const transformJsxInJs = () => ({
  name: 'transform-jsx-in-js',
  enforce: 'pre',
  async transform(code, id) {
    // Match only our source JS files containing JSX, excluding node_modules
    if (!id.match(/src\/.*\.js$/)) {
      return null;
    }
    return await transformWithOxc(code, id, {
      lang: 'jsx',
    });
  },
});

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [
    react(),
    transformJsxInJs(),
  ],
  server: {
    port: 3000,
  },
  test: {
    globals: true,
    environment: 'jsdom',
    setupFiles: './src/setupTests.js',
  },
});
