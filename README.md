# SAQ Inventory Scraper

This simple tool was created to query the quantities available across all available [SAQ](https://www.saq.com/) branches.

Since SAQ doesn't provide any public API, the only way to obtain this data is by manually browsing through the site.
An easy way to achieve this is to parse through the HTML page using a parser. However, since SAQ's product page doesn't 
load up the data for all stores at once, we need a way to automate the process of clicking certain buttons on page to
load the rest of the data.

This is where [Selenium Web Driver](https://www.selenium.dev/projects/) comes into action. This framework allows me to 
create scripts that automate the aforementioned processes, as well as scraping data from a given page.

### Requirements
You need to install [Chromedriver](https://chromedriver.chromium.org/home) before using this tool.

I have also provided some shell scripts in the releases to simplify this process.

### Usage guide

##### For macOS users

### Disclaimer
I created this tool in July 2020, during which the tool worked perfectly for the product pages in SAQ. However, their 
website design may change in the future and break the tool. I try to keep up with the design whenever possible but there's
no guarantee for it.
