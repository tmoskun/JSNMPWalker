#!/bin/bash

version=$(java -version 2>&1 |head -n 1 | cut -d\" -f 2 | sed -E 's|^([0-9])\.([0-9])(\..+)?$|\2|' | bc)
$(exit $version)
