# jobshop-minimal - A minimal jobshop planner

## Description:

A heuristic based approach is taken to solve the jobshop scheduling problem.
The primary goal is minimizing tardiness of demand.

Minimal support functionality includes:

* Modeling demands with priority, due date, due quantity
* Task sequence required to meet demand for a SKU
* Task setup time and per unit cycle times
* Tasks can load alternate workcenters (with priority sequence)
* Workcenters can model holidays and efficiency (to stretch tasks)

Other features are planned for the future but not listed at this time.
More details will be added to this README and to the project Wiki soon.

## Installation:

#### Prerequisites:

* JDK 8 (Oracle JDK or OpenJDK)
* Linux/MacOS environment which come with make and other utilities
* Windows users can download cygwin and modify the Makefile suitably
  although it is recommended that they use a Linux virtual machine

#### Compiling:

* Download a zip of the repository and unpack, or clone the repository.
* Open a terminal and cd into the root directory of the repository.
* Run "make all" and it should automatically compile the code.

## Usage:

* Open a terminal and cd into the root directory of the repository.
* Run the command: ** `java -jar bin/JobShop.jar -d <path to data directory>` **

#### Tips:

* Sample datasets are available in the "tests/testXXXX" directories

## Contributing:

The project is in its infancy with many moving and changing parts.
Therefore, no contributions are solicited; and very likely no contributions
will be accepted in the near future.  This README will be updated when this
policy changes.

## License:

This project is released under the AGPL version 3 license.  Please read through the LICENSE file
carefully and refer to the GNU website for more details on this license, or consult your lawyer.
