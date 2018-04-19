module.exports = {
  extends: 'stylelint-config-standard',
  rules: {
    'declaration-empty-line-before': null,
    'no-invalid-double-slash-comments': null,
    'number-leading-zero': 'never',
    'selector-pseudo-class-no-unknown': [
      true,
      {
        ignorePseudoClasses: ['global', 'local']
      }
    ],
    'selector-pseudo-element-colon-notation': 'single'
  }
};
