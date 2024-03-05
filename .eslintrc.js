module.exports = {
  root: true,
  parser: '@typescript-eslint/parser',
  env: { node: true, es6: true },
  plugins: ['@typescript-eslint'],
  extends: "prettier",
  rules: {
    curly: ['error', 'multi-line'],
    '@typescript-eslint/no-unused-vars': [
      'error',
      { argsIgnorePattern: '^_', destructuredArrayIgnorePattern: '^_' },
    ],
  },
};
