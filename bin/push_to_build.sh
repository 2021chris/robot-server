#bin/bash

git checkout build
git pull
git merge main --no-edit
git push
git checkout main
