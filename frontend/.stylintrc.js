const ALWAYS = {
  expect: 'always',
  error: true
};

const NEVER = {
  expect: 'never',
  error: true
};

module.exports = {
  reporter: 'stylint-stylish',
  reporterOptions: {
    verbose: true
  },
  noImportant: NEVER,
  semicolons: NEVER,
  blocks: false,
  brackets: NEVER,
  colons: NEVER,
  colors: ALWAYS,
  commaSpace: ALWAYS,
  commentSpace: ALWAYS,
  cssLiteral: NEVER,
  customProperties: [],
  depthLimit: false,
  duplicates: NEVER,
  efficient: ALWAYS,
  exclude: ['styles/bootstrap/**/*.styl'],
  extendPref: false,
  globalDupe: false,
  groupOutputByFile: true,
  indentPref: false,
  leadingZero: NEVER,
  maxErrors: false,
  maxWarnings: false,
  mixed: false,
  mixins: [],
  namingConvention: false,
  namingConventionStrict: false,
  none: NEVER,
  parenSpace: false,
  placeholders: ALWAYS,
  prefixVarsWithDollar: ALWAYS,
  quotePref: false,
  stackedProperties: NEVER,
  trailingWhitespace: NEVER,
  universal: false,
  valid: true,
  zeroUnits: NEVER,
  zIndexNormalize: false
};
