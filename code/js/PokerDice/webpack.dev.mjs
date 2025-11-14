import path from 'path';
import tailwindcss from 'tailwindcss';
import autoprefixer from 'autoprefixer';

export default {
  mode: 'development',
  devServer: {
    historyApiFallback: true,
    hot: false,
    liveReload: false,
    webSocketServer: false,
    compress: false,
    proxy: [
      {
        context: ['/api'],
        target: 'http://localhost:8081',
        onProxyRes: (proxyRes, req, res) => {
          proxyRes.on('close', () => {
            if (!res.writableEnded) {
              res.end();
            }
          });
          res.on('close', () => {
            proxyRes.destroy();
          });
        },
      },
    ],
  },
  resolve: {
    extensions: ['.js', '.ts', '.tsx', '.css'], 
  },
  plugins: [],
  module: {
    rules: [
      {
        test: /\.tsx?$/,
        use: 'ts-loader',
        exclude: /node_modules/,
      },
      {
        test: /\.css$/,
        use: [
          'style-loader',
          'css-loader',
          {
            loader: 'postcss-loader',
            options: {
              postcssOptions: {
                plugins: [
                  tailwindcss,
                  autoprefixer,
                ],
              },
            },
          },
        ],
      },
    ],
  },
};