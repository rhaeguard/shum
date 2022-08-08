pipenv run python main-llvm.py | sed -e 's/unknown-unknown-unknown/x86_64-pc-linux-gnu/g' | lli
echo $?