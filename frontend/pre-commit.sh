#!/bin/bash
echo "start lint:stage"

# 如果有进入node_modules目录的权限则遍历修改后的文件，否则'npm run lint:fix'
if [ -x node_modules ]; then
  for file in $(git diff HEAD --name-only | grep -E '\.(js|jsx|vue)$')
  do
    node_modules/.bin/eslint --fix "$file"
    if [ $? -ne 0 ]; then
      echo "ESLint failed on staged file '$file'. Please check your code and try again. You can run ESLint manually via npm run eslint."
      exit 1
    fi
  done
else
  npm run lint:fix
  if [ $? -ne 0 ]; then
      echo "ESLint failed."
      exit 1
  fi
fi

npm run puglint
if [ $? -ne 0 ]; then
  echo "puglint failed."
  exit 1
fi

npm run stylelint
if [ $? -ne 0 ]; then
  echo "stylelint failed."
  exit 1
fi
