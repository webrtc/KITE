module.exports = {
  env: {
    node: true,
    browser: true
  },
  plugins: [
    'import'
  ],
  extends: [
    'standard',
    'plugin:import/errors',
    'plugin:import/warnings'
  ]
}
