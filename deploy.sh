DIR        = "$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
DEPLOY_DIR = "/deploy/"
YAML       = "application.yaml"
PARSER     = "yaml_parse.sh"

eval ". \"$DIR$DEPLOY_DIR$PARSER\" "

yaml_parse "${DIR}${DEPLOY_DIR}${YAML}" & echo
create_variables "${DIR}${DEPLOY_DIR}${YAML}"
