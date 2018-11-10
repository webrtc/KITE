const webpack = require('webpack')

module.exports = {
  mode: 'production',
  entry: {
    simulcast: './src/client/simulcast',
    broadcast: './src/client/broadcast'
  },
  output: {
    filename: '[name].js',
    path: `${__dirname}/public/js`
  },
  plugins: [
    new webpack.SourceMapDevToolPlugin({
      filename: '[name].js.map',
      publicPath: '/js/'
    })
  ]
}
