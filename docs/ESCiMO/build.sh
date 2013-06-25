#!/bin/bash
#

latex final_report.tex
bibtex final_report
latex final_report.tex
latex final_report.tex
dvipdf final_report.dvi
