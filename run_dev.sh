#!/bin/bash
# shellcheck source=/dev/null
source .env
multitail -cT ansi -s 2 -l 'gw run -t' -cT ansi -l '(cd client && pnpm quasar dev)'
