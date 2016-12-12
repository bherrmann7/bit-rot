

# bit-rot

This project is still in "just started" phase

The goal is to watch over photo directories / archive directories and detect bit rot.

# The Plan

The search recursively for all files in a number of base directories
compute the md5 of all files
compare the md5 of a previous run
report via sending an email on any changes and/or new files
set bit-rot to run from cron weekly

