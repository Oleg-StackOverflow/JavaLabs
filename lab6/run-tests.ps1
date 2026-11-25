# Компіляція і запуск тестів у Windows PowerShell. Запуск: .\run-tests.ps1
$ErrorActionPreference = "Stop"
$cp = "out/main;out/test;lib/junit-4.13.2.jar;lib/hamcrest-core-1.3.jar"
if (Test-Path out) { Remove-Item -Recurse -Force out }
javac -encoding UTF-8 -Xlint:all -Werror -d out/main (Get-ChildItem -Recurse -Filter *.java src).FullName
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
javac -encoding UTF-8 -d out/test -cp $cp (Get-ChildItem -Recurse -Filter *.java test).FullName
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
java -cp $cp org.junit.runner.JUnitCore rbtree.RedBlackTreeTest rbtree.TreePrinterTest
