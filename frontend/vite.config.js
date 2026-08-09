import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { fileURLToPath } from 'url';
import path from 'path';

const __filename = fileURLToPath(import.meta.url);

export default defineConfig({
    plugins: [vue()],
    root: './',  // 确保根目录正确
     base: './',// 或者 '/'
    build: {
        rollupOptions: {
            input: 'index.html' // 指定入口文件
        }
    },
    resolve: {
        alias: {
            '@': '/src', // 确保这里的路径是正确的
        },
    },
    server: {
        open: true, // 启动时自动打开浏览器
    }

});
