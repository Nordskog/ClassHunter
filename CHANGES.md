# Changelog
All notable changes to this project will be documented in this file.  
Dates are in the format dd/mm/yyyy

## [1.3.2] - 18-03-2020
### Changed
- Catch any errors ( other than ClassNotFound ) thrown during class comparison, return score of 0 for that component

## [1.3.0] - 22-09-2019
### Changed
- Class profile scores weighted for more accurate results in real-world scenarios
- Modifiers now compute a proper similarity score instead of relying on a perfect match.
- Class Profile now uses simplified similarity score to break early ( Performance improved, but overall same because of improved accuracy )

## [1.2.0] - 20-08-2017
### Changed
- Class search will skip remaining comparisons if it becomes impossible for the similary score to match or surpass that of existing result(s), greatly increasing performance.

## [1.1.0] - 29-11-2017
### Changed
- Lists now perform nested similarity calculations. Very slow, but far more accurate.

## [1.0.0] - 29-07-2017
### Added
- Initial Release
