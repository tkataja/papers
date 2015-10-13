# papers
Clojurescript/Planck script to fetch and set wallpaper on Mac OS X (Yosemite+)

## Why?
Searching of great-looking wallpapers and setting them is a tedious task.

## TODO

### MVP
- [x] Fetch random wallpaper 
- [x] Automatically set it as a background image on Mac OS X

### Nice to have
- [x] CLI option to fetch wallpaper using given topic
- [x] CLI option to allow download-only
- [ ] Set profile (15" Retina Macbook, iPhone6 etc.) from command line

### Things to do to be taken seriously
- [ ] Do proper error-handling
- [ ] Minimize dependencies on external tools (curl, wget, printenv, pwd etc.)
- [ ] Examine if there exists some great API for wallpaper-fetching and use it instead of scraping papers.co
