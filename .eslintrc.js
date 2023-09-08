module.exports = {
  root: true,
  parser: '@typescript-eslint/parser',
  env: { node: true, es6: true },
  plugins: ['@typescript-eslint'],
  extends: [
    '@react-native-community',
    'plugin:@typescript-eslint/recommended',
    'plugin:prettier/recommended',
  ],
  rules: {
    '@typescript-eslint/explicit-module-boundary-types': 'off',
    '@typescript-eslint/no-empty-interface': 'off',
  },
};
