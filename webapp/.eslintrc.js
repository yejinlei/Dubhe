module.exports = {
  root: true,
  parserOptions: {
    parser: "babel-eslint",
    sourceType: "module"
  },
  env: {
    browser: true,
    node: true,
    es6: true
  },
  extends: ["airbnb-base", "plugin:vue/recommended", "eslint-config-prettier", "prettier/vue"],
  plugins: ["import",  "eslint-plugin-prettier"],
  settings: {
    'import/resolver': {
       webpack: {
         config: require.resolve('@vue/cli-service/webpack.config.js') 
       },
    }
  },
  rules: {
    "comma-dangle": [2, "always-multiline"],
    "no-var": "error",
    "no-console": [2, { allow: ["warn", "error"] }],
    "no-restricted-syntax": "off",
    "no-underscore-dangle": "off",
    "guard-for-in": "off",
    "object-shorthand": 2,
    "consistent-return": "off",
    "no-multi-assign": "off",
    "no-shadow": "off",
    "no-restricted-globals": "off",
    "no-restricted-properties": "off",
    "no-prototype-builtins": "off",
    "no-unused-vars": [
      2,
      { ignoreRestSiblings: true, argsIgnorePattern: "^h$" }
    ],
    "import/prefer-default-export": "off",
    'import/extensions': ['error', 'always', {
      'js': 'never',
      'vue': 'never'
    }],
    "no-unused-expressions": "off",
    "no-undef": 2,
    camelcase: "off",
    "no-extra-boolean-cast": "off",
    "no-param-reassign":"off",
    semi: ["error", "always"],
    "vue/require-prop-types": "off",
    "vue/require-default-prop": "off",
    "vue/no-reserved-keys": "off",
    "vue/attribute-hyphenation": "off",
    "vue/comment-directive": "off",
    "vue/prop-name-casing": "off",
    "vue/max-attributes-per-line": [
      2,
      {
        singleline: 20,
        multiline: {
          max: 1,
          allowFirstLine: false
        }
      }
    ]
  }
};
