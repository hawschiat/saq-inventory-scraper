#!/bin/sh

cd "$(dirname "$0")"

# check if Java is installed
if test -z "$(which java)"
then
  echo "Java is not installed."
  exit 1;
fi

# check if homebrew is installed
if test -z "$(which brew)"
then
  # install homebrew
  curl -fsSL https://raw.githubusercontent.com/Homebrew/install/master/install.sh | sh
fi

# check if chromedriver is installed
if test -z "$(brew cask list chromedriver)"
then
  # install chromedriver
  brew cask install chromedriver
fi

# run jar file
java -jar saq-inventory-scraper-0.1.0.jar
