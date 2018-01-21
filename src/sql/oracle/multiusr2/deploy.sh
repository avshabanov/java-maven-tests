# !/bin/bash
################################################################################

#
# Database schema deployer script.
#
# Creates temporary SQL files with the substituted variables.
# Substitution is better from the readability's point of view, so no arguments
# passed to the sqlplus except for the script name and credentials.
#
# Author: Alexander Shabanov, 2011
#


#
DB_DBA_LOGIN='SYS'
DB_DBA_PASSWORD=

# Deployed schema parameters
S_SCHEMA=
S_SCHEMA_PASSWORD=
S_TABLESPACE=
S_TABLESPACE_SIZE='1M'
S_DBUSER_NAME=
S_DBUSER_PASSWORD=

# Schema suffix added to the schema parameters
S_SUFFIX_INDEX=

# Flag that indicates whether to keep SQL (1) or not (0)
F_KEEP_SQL=0

# Flag that indicates whether undeploy scenario is needed
# 0 - do deploy, 1 - do undeploy
F_UNDEPLOY=0

# Defaults (used as concatenation with index)
C_DEF_SCHEMA='qwa'
C_DEF_SCHEMA_PASSWORD='test'
C_DEF_TABLESPACE='qwa_tspace'
C_DEF_DBUSER_NAME='qwa_user'
C_DEF_DBUSER_PASSWORD='qwatest'


# The function prints script usage parameters and exits
function printUsageAndExit {
    echo "usage: ./unwrap.sh --dba-login {LOGIN} --dba-password {PASSWORD} --index 1"
    echo
    echo "Arguments:"
    echo "  --dba-login     {LOGIN}     Optional oracle DBA login name."
    echo "                              Default value is SYS, connected as SYSDBA."
    echo "  --dba-password  {PASSWORD}  Required oracle DBA password."
    echo "  --index         {INDEX}     Required suffix index."
    echo "                              Appended as a suffix to all the schema names."
    echo "  --keep-sql                  Keep generated SQL files."
    echo "  --undeploy                  Undeploy previously deployed scheme."

    exit
}

# Assigns schema parameters by using schema suffix index
function assingSchemaParametersUsingIndex {
    if [ -z $1 ] ; then
        echo "Error: schema suffix index is null."
        printUsageAndExit
    fi
    
    S_SCHEMA="${C_DEF_SCHEMA}$1"
    S_SCHEMA_PASSWORD="${C_DEF_SCHEMA_PASSWORD}"
    S_TABLESPACE="${C_DEF_TABLESPACE}$1"
    S_DBUSER_NAME="${C_DEF_DBUSER_NAME}$1"
    S_DBUSER_PASSWORD="${C_DEF_DBUSER_PASSWORD}"
}

# Prints assigned parameters
function printParameters {
    echo "========================================"
    echo "Using parameters:"
    echo
    echo "SCHEMA:           $S_SCHEMA"
    echo "SCHEMA PASSWORD:  $S_SCHEMA_PASSWORD"
    echo "TABLESPACE:       $S_TABLESPACE"
    echo "TABLESPACE SIZE:  $S_TABLESPACE_SIZE"
    echo "USER NAME:        $S_DBUSER_NAME"
    echo "USER PASSWORD:    $S_DBUSER_PASSWORD"
    echo "========================================"
}

# Extracts command line arguments
function extractArguments {
    local PREV
    local PARAM
    
    for PARAM in "$@"
    do
        # extract S_SCHEMA
        if [ "$PREV" == '--dba-login' ] ; then
            DB_DBA_LOGIN=$PARAM
            PREV=''
            continue
        fi
        
        # extract S_SCHEMA
        if [ "$PREV" == '--dba-password' ] ; then
            DB_DBA_PASSWORD=$PARAM
            PREV=''
            continue
        fi
        
        # extract S_SUFFIX_INDEX
        if [ "$PREV" == '--index' ] ; then
            S_SUFFIX_INDEX=$PARAM
            PREV=''
            continue
        fi
        
        # extract F_KEEP_SQL
        if [ "$PARAM" == '--keep-sql' ] ; then
            F_KEEP_SQL=1
            PREV=''
            continue
        fi

        # extract F_UNDEPLOY
        if [ "$PARAM" == '--undeploy' ] ; then
            F_UNDEPLOY=1
            PREV=''
            continue
        fi
        
        # init PREV param
        PREV=$PARAM
    done
} # extractArguments


# executes script in sqlplus utility
# $1    DB user name
# $2    DB user password
# $3    DB user role (can be empty)
# $4    Script contents
# Note: indentation is intentionally omitted due to bash syntax limitations!
function execSql {
    # run utility
    sqlplus -s "${1}/${2}" ${3} "@${4}" <<EOF
exit;
EOF
    

    # check result
    local RET=$?
    if [ $RET -ne 0 ]
    then
        echo "Script failed, error code = $RET"
        exit $RET
    fi
}


# Constant: temporary SQL files list
TEMP_SQL_FILES=

# Sed pattern to replace the script placeholders to the sane values
SED_PATTERN=

function initSedPattern {
    SED_PATTERN="s/\$sizeof_tablespace/${S_TABLESPACE_SIZE}/g"
    SED_PATTERN="${SED_PATTERN};s/\$schema_password/${S_SCHEMA_PASSWORD}/g"
    SED_PATTERN="${SED_PATTERN};s/\$schema/$S_SCHEMA/g"
    SED_PATTERN="${SED_PATTERN};s/\$sizeof_tablespace/${S_TABLESPACE_SIZE}/g"
    SED_PATTERN="${SED_PATTERN};s/\$tablespace/${S_TABLESPACE}/g"
    SED_PATTERN="${SED_PATTERN};s/\$dbuser_password/${S_DBUSER_PASSWORD}/g"
    SED_PATTERN="${SED_PATTERN};s/\$dbuser_name/${S_DBUSER_NAME}/g"
}

# Removes all the generated scripts
function deleteScripts {
    for TEMP_FILE in $TEMP_SQL_FILES
    do
        if [ -f $TEMP_FILE ]; then
            echo "Deleting temporary $TEMP_FILE"
            rm $TEMP_FILE
            local STATUS=$?

            if [ $STATUS -ne 0 ]; then
                echo "Permission denied: can't delete $TEMP_FILE."
                exit
            fi
        fi
    done
}

# Checks that generated files exist
function checkScriptsExist {
    # print all the files contents: find /tmp/ -name "*-tmp.sql" 2> /dev/null | xargs cat
    
    # check all the files exist
    for TEMP_FILE in $TEMP_SQL_FILES
    do
        if [ ! -f $TEMP_FILE ]; then
            echo "Permission denied: can't write to $TEMP_FILE."
            exit
        fi
    done
}


# Creates deploy scripts.
function createDeployScripts {
    # execute scripts
    sed $SED_PATTERN "./sql/schema/create-users.sql" > "/tmp/create-users-tmp.sql"    
    sed $SED_PATTERN "./sql/schema/create-tables.sql" > "/tmp/create-tables-tmp.sql"
    sed $SED_PATTERN "./sql/schema/procedures.sql" > "/tmp/procedures-tmp.sql"
    sed $SED_PATTERN "./sql/schema/views.sql" > "/tmp/views-tmp.sql"
}

# Executes the generated script in the Oracle's sqlplus
function runDeployScripts {
    echo "[1] Creating users related to schema $S_SCHEMA as user $DB_DBA_LOGIN"
    execSql $DB_DBA_LOGIN $DB_DBA_PASSWORD "AS SYSDBA" "/tmp/create-users-tmp.sql"
    echo "[1] OK"
    
    echo "[2] Creating tables for $S_SCHEMA as $S_SCHEMA/$S_SCHEMA_PASSWORD"
    execSql $S_SCHEMA $S_SCHEMA_PASSWORD "" "/tmp/create-tables-tmp.sql"
    echo "[2] OK"

    echo "[3] Creating views for $S_SCHEMA as user $S_SCHEMA"
    execSql $S_SCHEMA $S_SCHEMA_PASSWORD "" "/tmp/views-tmp.sql"
    echo "[3] OK"

    echo "[4] Creating api for $S_SCHEMA as user $S_SCHEMA"
    execSql $S_SCHEMA $S_SCHEMA_PASSWORD "" "/tmp/procedures-tmp.sql"
    echo "[4] OK"
}

# Creates undeploy scripts.
function createUndeployScripts {
    # execute scripts
    sed $SED_PATTERN "./sql/drop/drop.sql" > "/tmp/drop-tmp.sql"    
}

# Executes the generated script in the Oracle's sqlplus
function runUndeployScripts {
    echo "[1] Dropping schema $S_SCHEMA as user $DB_DBA_LOGIN"
    execSql $DB_DBA_LOGIN $DB_DBA_PASSWORD "AS SYSDBA" "/tmp/drop-tmp.sql"
    echo "[1] OK"
    
    exit
}

#
# Script entry point
#

# Welcome prompt
echo "Database Schema Deployer."

# Prepare S_xxx variables
extractArguments $@
assingSchemaParametersUsingIndex $S_SUFFIX_INDEX
printParameters
initSedPattern

# Pick variables depending on the deploy/undeploy scenario
if [ $F_UNDEPLOY -eq 0 ]
then
    TEMP_SQL_FILES="/tmp/create-users-tmp.sql /tmp/create-tables-tmp.sql /tmp/procedures-tmp.sql /tmp/views-tmp.sql"
else
    TEMP_SQL_FILES="/tmp/drop-tmp.sql"
fi

# Remove previous scripts if any
deleteScripts


# Generate and run scripts
if [ $F_UNDEPLOY -eq 0 ]
then
    createDeployScripts
    checkScriptsExist
    runDeployScripts
else
    createUndeployScripts
    checkScriptsExist
    runUndeployScripts
fi

# Keep scripts if needed
if [ $F_KEEP_SQL -eq 0 ]
then
    deleteScripts
fi

# All OK
echo "OK: schema $S_SCHEMA has been deployed"
exit

