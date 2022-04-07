# Assumes build is done up to 'mvn package'
# and there are no outdated jars in the target directory
jar=$(echo /mnt/c/olivine/target/olivine-*.jar)
python /mnt/c/olivine/etc/test-prover.py "$HOME/jdk-17/bin/java -cp $jar olivine/Prover" $@
