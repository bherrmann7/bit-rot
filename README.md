

## This is a personal project is to assist in the management of a large collection of photos.

Eventually this suite of programs should do;

- bit-rot: report on changes to a hash of image file (This is to catch bit rot before it is distributed to backups.)
 
- bit-dupe: help eliminate accidental duplicates of files
 

# bit-rot

generate a report of a filesystems hashs

# The Plan

The search recursively for all files in a number of base directories
compute the md5 of all files
save the md5s into a dated record

# bit-diff

compare the md5 of the last two runs of bit-rot.

Send the diff report as an email.

The plan is to set bit-rot to run from cron weekly



