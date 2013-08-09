@echo off
set nullGapp="C:\Users\dlutz\git\opensextant\LanguageResources\GAPPs\OpenSextant_NULL.gapp"
set geocoordGapp="C:\Users\dlutz\git\opensextant\LanguageResources\GAPPs\OpenSextant_GeocoordsOnly.gapp"
set basicGapp="C:\Users\dlutz\git\opensextant\LanguageResources\GAPPs\OpenSextant_Basic.gapp"
set noSolrGapp="C:\Users\dlutz\git\opensextant\LanguageResources\GAPPs\OpenSextant_Solr_NO_NAIVETAGGER.gapp"
set SolrOnlyGapp="C:\Users\dlutz\git\opensextant\LanguageResources\GAPPs\OpenSextant_Solr_SOLR_ONLY.gapp"
set geotaggerGapp="C:\Users\dlutz\git\opensextant\LanguageResources\GAPPs\OpenSextant_Solr.gapp"
set gpGapp="C:\Users\dlutz\git\opensextant\LanguageResources\GAPPs\OpenSextant_GeneralPurpose.gapp"

set tinyDir=".\testData\tiny" 
set newsDir=".\testData\news" 
set osTestDir=".\testData\openSextant_TXT"
set adHocTestDir=".\testData\adhoc"

echo NumberDocs	numberCharacters	numberAnnotations	totalDuration	initDuration	rate	GAPP	TestSet	memory

REM NULL
REM FOR %%m in (2m,3m,4m,5m,10m,20m,50m,75m,100m,200m,300m,400m,500m,1024m,2048m,3072m) Do java -Xms%%m -Xmx%%m -Dsolr.solr.home=C:/mitre/opensextant/solr -classpath .\build\GATETestDriver.jar;.\lib\*;.\plugins\OpenSextantToolbox\lib\*;.\etc  org.mitre.opensextant.GATETestDriver  %nullGapp% %tinyDir% %%m
REM FOR %%m in (2m,3m,4m,5m,10m,20m,50m,75m,100m,200m,300m,400m,500m,1024m,2048m,3072m) Do java -Xms%%m -Xmx%%m -Dsolr.solr.home=C:/mitre/opensextant/solr -classpath  .\build\GATETestDriver.jar;.\lib\*;.\plugins\OpenSextantToolbox\lib\*;.\etc  org.mitre.opensextant.GATETestDriver  %nullGapp% %newsDir% %%m
REM FOR %%m in (2m,3m,4m,5m,10m,20m,50m,75m,100m,200m,300m,400m,500m,1024m,2048m,3072m) Do java -Xms%%m -Xmx%%m -Dsolr.solr.home=C:/mitre/opensextant/solr -classpath  .\build\GATETestDriver.jar;.\lib\*;.\plugins\OpenSextantToolbox\lib\*;.\etc  org.mitre.opensextant.GATETestDriver  %nullGapp% %osTestDir% %%m

REM geocoord only
REM FOR %%m in (2m,3m,4m,5m,10m,20m,50m,75m,100m,200m,300m,400m,500m,1024m,2048m,3072m) Do java -Xms%%m -Xmx%%m -Dsolr.solr.home=C:/mitre/opensextant/solr -classpath .\build\GATETestDriver.jar;.\lib\*;.\plugins\OpenSextantToolbox\lib\*;.\etc  org.mitre.opensextant.GATETestDriver  %geocoordGapp% %tinyDir% %%m
REM FOR %%m in (2m,3m,4m,5m,10m,20m,50m,75m,100m,200m,300m,400m,500m,1024m,2048m,3072m) Do java -Xms%%m -Xmx%%m -Dsolr.solr.home=C:/mitre/opensextant/solr -classpath  .\build\GATETestDriver.jar;.\lib\*;.\plugins\OpenSextantToolbox\lib\*;.\etc  org.mitre.opensextant.GATETestDriver  %geocoordGapp% %newsDir% %%m
REM FOR %%m in (2m,3m,4m,5m,10m,20m,50m,75m,100m,200m,300m,400m,500m,1024m,2048m,3072m) Do java -Xms%%m -Xmx%%m -Dsolr.solr.home=C:/mitre/opensextant/solr -classpath  .\build\GATETestDriver.jar;.\lib\*;.\plugins\OpenSextantToolbox\lib\*;.\etc  org.mitre.opensextant.GATETestDriver  %geocoordGapp% %osTestDir% %%m

REM FOR %%m in (2m,3m,4m,5m,10m,20m,50m,75m,100m,200m,300m,400m,500m,1024m,2048m,3072m) Do java -Xms%%m -Xmx%%m -Dsolr.solr.home=C:/mitre/opensextant/solr -classpath  .\build\GATETestDriver.jar;.\lib\*;.\plugins\OpenSextantToolbox\lib\*;.\etc  org.mitre.opensextant.GATETestDriver  %geocoordGapp% %adhocTestDir% %%m



REM Basic
REM FOR %%m in (2m,3m,4m,5m,10m,20m,50m,75m,100m,200m,300m,400m,500m,1024m,2048m,3072m) Do java -Xms%%m -Xmx%%m -Dsolr.solr.home=C:/mitre/opensextant/solr -classpath  .\build\GATETestDriver.jar;.\lib\*;.\plugins\OpenSextantToolbox\lib\*;.\etc  org.mitre.opensextant.GATETestDriver  %basicGapp% %tinyDir% %%m
REM FOR %%m in (2m,3m,4m,5m,10m,20m,50m,75m,100m,200m,300m,400m,500m,1024m,2048m,3072m) Do java -Xms%%m -Xmx%%m -Dsolr.solr.home=C:/mitre/opensextant/solr -classpath  .\build\GATETestDriver.jar;.\lib\*;.\plugins\OpenSextantToolbox\lib\*;.\etc  org.mitre.opensextant.GATETestDriver  %basicGapp% %newsDir% %%m
REM FOR %%m in (2m,3m,4m,5m,10m,20m,50m,75m,100m,200m,300m,400m,500m,1024m,2048m,3072m) Do java -Xms%%m -Xmx%%m -Dsolr.solr.home=C:/mitre/opensextant/solr -classpath  .\build\GATETestDriver.jar;.\lib\*;.\plugins\OpenSextantToolbox\lib\*;.\etc  org.mitre.opensextant.GATETestDriver  %basicGapp% %osTestDir% %%m

REM geotagger NO SOLR
REM FOR %%m in (300m,500m,1024m,2048m,3072m) Do java -Xms%%m -Xmx%%m -Dsolr.solr.home=C:/mitre/opensextant/solr -classpath  .\build\GATETestDriver.jar;.\lib\*;.\plugins\OpenSextantToolbox\lib\*;.\etc  org.mitre.opensextant.GATETestDriver  %noSolrGapp% %tinyDir% %%m
REM FOR %%m in (100m) Do java -Xms%%m -Xmx%%m -Dsolr.solr.home=C:/mitre/opensextant/solr -classpath  .\build\GATETestDriver.jar;.\lib\*;.\plugins\OpenSextantToolbox\lib\*;.\etc  org.mitre.opensextant.GATETestDriver  %noSolrGapp% %newsDir% %%m
REM FOR %%m in (70m,75m,100m,200m,300m,400m,500m,1024m,2048m,3072m) Do java -Xms%%m -Xmx%%m -Dsolr.solr.home=C:/mitre/opensextant/solr -classpath  .\build\GATETestDriver.jar;.\lib\*;.\plugins\OpenSextantToolbox\lib\*;.\etc  org.mitre.opensextant.GATETestDriver  %noSolrGapp% %osTestDir% %%m

REM Solr Only
REM FOR %%m in (400m,500m,1024m,2048m,3072m) Do java -Xms%%m -Xmx%%m -Dsolr.solr.home=C:/mitre/opensextant/solr -classpath  .\build\GATETestDriver.jar;.\lib\*;.\plugins\OpenSextantToolbox\lib\*;.\etc  org.mitre.opensextant.GATETestDriver  %SolrOnlyGapp% %tinyDirr% %%m
REM FOR %%m in (100m) Do java -Xms%%m -Xmx%%m -Dsolr.solr.home=C:/mitre/opensextant/solr -classpath  .\build\GATETestDriver.jar;.\lib\*;.\plugins\OpenSextantToolbox\lib\*;.\etc  org.mitre.opensextant.GATETestDriver  %SolrOnlyGapp% %newsDir% %%m
REM FOR %%m in (400m,500m,1024m,2048m,3072m) Do java -Xms%%m -Xmx%%m -Dsolr.solr.home=C:/mitre/opensextant/solr -classpath  .\build\GATETestDriver.jar;.\lib\*;.\plugins\OpenSextantToolbox\lib\*;.\etc  org.mitre.opensextant.GATETestDriver  %SolrOnlyGapp% %osTestDir% %%m

REM geotagger
REM FOR %%m in (50m,75m,100m,200m,300m) Do java -Xms%%m -Xmx%%m -Dsolr.solr.home=C:/mitre2/opensextant -classpath  .\build\GATETestDriver.jar;.\lib\*;.\plugins\OpenSextantToolbox\lib\*;.\etc  org.mitre.opensextant.GATETestDriver  %geotaggerGapp% %tinyDir% %%m
REM FOR %%m in (500m,1024m,2048m,3072m) Do java -Xms%%m -Xmx%%m -Dsolr.solr.home=C:/mitre/opensextant/solr -classpath  .\build\GATETestDriver.jar;.\lib\*;.\plugins\OpenSextantToolbox\lib\*;.\etc  org.mitre.opensextant.GATETestDriver  %geotaggerGapp% %newsDir% %%m
FOR %%m in (100m,500m,1024m,2048m,3072m) Do java -Xms%%m -Xmx%%m -Dopensextant.home=C:/Users/dlutz/git/opensextant/OpenSextantToolbox/release -Dsolr.solr.home=C:/mitre_basicPlus/opensextant -classpath  .\build\GATETestDriver.jar;.\lib\*;.\plugins\OpenSextantToolbox\lib\*;.\etc\*;.\etc  org.mitre.opensextant.GATETestDriver  %geotaggerGapp% %osTestDir% %%m

REM General Purpose FULL
REM FOR %%m in (1024m,2048m,3072m) Do java -Xms%%m -Xmx%%m -Dsolr.solr.home=C:/mitre/opensextant/solr -classpath  .\build\GATETestDriver.jar;.\lib\*;.\plugins\OpenSextantToolbox\lib\*;.\etc  org.mitre.opensextant.GATETestDriver  %gpGapp% %tinyDir% %%m
REM FOR %%m in (1024m,2048m,3072m) Do java -Xms%%m -Xmx%%m -Dsolr.solr.home=C:/mitre/opensextant/solr -classpath  .\build\GATETestDriver.jar;.\lib\*;.\plugins\OpenSextantToolbox\lib\*;.\etc  org.mitre.opensextant.GATETestDriver  %gpGapp% %newsDir% %%m
REM FOR %%m in (1024m,2048m,3072m) Do java -Xms%%m -Xmx%%m -Dsolr.solr.home=C:/mitre/opensextant/solr -classpath  .\build\GATETestDriver.jar;.\lib\*;.\plugins\OpenSextantToolbox\lib\*;.\etc  org.mitre.opensextant.GATETestDriver  %gpGapp% %osTestDir% %%m
