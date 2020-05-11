# Changelog

## [v1.7.2](https://github.com/sbt/sbt-native-packager/tree/v1.7.2) (2020-05-11)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.7.1...v1.7.2)

**Fixed bugs:**

- AshScriptPlugin with DockerPlugin does not correctly pass arguments [\#1332](https://github.com/sbt/sbt-native-packager/issues/1332)

**Closed issues:**

- JavaAppPackaging enables Docker packaging [\#1337](https://github.com/sbt/sbt-native-packager/issues/1337)
- Docker Entrypoint does not consider mainClass option \(and resulted script name\) [\#1335](https://github.com/sbt/sbt-native-packager/issues/1335)

**Merged pull requests:**

- Add support for docker:publish in DockerSpotifyClientPlugin [\#1338](https://github.com/sbt/sbt-native-packager/pull/1338) ([pdalpra](https://github.com/pdalpra))

## [v1.7.1](https://github.com/sbt/sbt-native-packager/tree/v1.7.1) (2020-05-04)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.7.0...v1.7.1)

**Fixed bugs:**

- missing libraries in final package [\#1028](https://github.com/sbt/sbt-native-packager/issues/1028)

**Closed issues:**

- Creating native-package for tests [\#1336](https://github.com/sbt/sbt-native-packager/issues/1336)
- Version check not working for openjdk 11 [\#1331](https://github.com/sbt/sbt-native-packager/issues/1331)
- input too long for win10 [\#1327](https://github.com/sbt/sbt-native-packager/issues/1327)
- Customizing Docker image fails when container environment variables are used [\#1324](https://github.com/sbt/sbt-native-packager/issues/1324)
- Cannot run windows:packageBin [\#1322](https://github.com/sbt/sbt-native-packager/issues/1322)
- The second FROM stage should be named [\#1315](https://github.com/sbt/sbt-native-packager/issues/1315)
- docker: introduce more layers for smaller images [\#1267](https://github.com/sbt/sbt-native-packager/issues/1267)

**Merged pull requests:**

- Fix argument passing in ash template [\#1334](https://github.com/sbt/sbt-native-packager/pull/1334) ([bentucker](https://github.com/bentucker))
- Add space between inline-literal symbol and word [\#1328](https://github.com/sbt/sbt-native-packager/pull/1328) ([sshark](https://github.com/sshark))
- Improved layer grouping [\#1326](https://github.com/sbt/sbt-native-packager/pull/1326) ([jroper](https://github.com/jroper))
- Update universal.rst [\#1320](https://github.com/sbt/sbt-native-packager/pull/1320) ([jlncrnt](https://github.com/jlncrnt))
- Remove docker sudo test from build [\#1318](https://github.com/sbt/sbt-native-packager/pull/1318) ([jroper](https://github.com/jroper))
- Name the main docker stage [\#1316](https://github.com/sbt/sbt-native-packager/pull/1316) ([jroper](https://github.com/jroper))

## [v1.7.0](https://github.com/sbt/sbt-native-packager/tree/v1.7.0) (2020-03-16)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.6.2...v1.7.0)

**Fixed bugs:**

- Release of 1.6.2 failed [\#1313](https://github.com/sbt/sbt-native-packager/issues/1313)

**Merged pull requests:**

- Separate Docker Layers for Dependencies and App jars. [\#1310](https://github.com/sbt/sbt-native-packager/pull/1310) ([ppiotrow](https://github.com/ppiotrow))

## [v1.6.2](https://github.com/sbt/sbt-native-packager/tree/v1.6.2) (2020-03-15)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.6.1...v1.6.2)

**Fixed bugs:**

- AshScriptPlugin uses bash syntax to process args [\#1302](https://github.com/sbt/sbt-native-packager/issues/1302)

**Closed issues:**

- how to hook proguard into dist [\#1309](https://github.com/sbt/sbt-native-packager/issues/1309)
- docker:publishlocal use results from publishlocal? [\#1308](https://github.com/sbt/sbt-native-packager/issues/1308)

**Merged pull requests:**

- Fix windows / test-bat-template [\#1314](https://github.com/sbt/sbt-native-packager/pull/1314) ([sshark](https://github.com/sshark))
- Fix for native-image to work in Windows [\#1312](https://github.com/sbt/sbt-native-packager/pull/1312) ([sshark](https://github.com/sshark))
- please add sbt-kubeyml [\#1306](https://github.com/sbt/sbt-native-packager/pull/1306) ([vaslabs](https://github.com/vaslabs))
- Fix ash template [\#1303](https://github.com/sbt/sbt-native-packager/pull/1303) ([gregsymons](https://github.com/gregsymons))
- Change all references to macOS [\#1301](https://github.com/sbt/sbt-native-packager/pull/1301) ([dwijnand](https://github.com/dwijnand))

## [v1.6.1](https://github.com/sbt/sbt-native-packager/tree/v1.6.1) (2020-01-30)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.6.0...v1.6.1)

**Closed issues:**

- docker image build not honoring API version [\#1294](https://github.com/sbt/sbt-native-packager/issues/1294)
- Expose the generated rpm filepath [\#1287](https://github.com/sbt/sbt-native-packager/issues/1287)

**Merged pull requests:**

- Fix little typo in longclasspath.rst [\#1300](https://github.com/sbt/sbt-native-packager/pull/1300) ([romankarlstetter](https://github.com/romankarlstetter))
- Introduce RPM / packageBin / artifactPath setting. [\#1299](https://github.com/sbt/sbt-native-packager/pull/1299) ([Lasering](https://github.com/Lasering))
- Enable docker for github actions, remove a bunch of travis jobs [\#1298](https://github.com/sbt/sbt-native-packager/pull/1298) ([muuki88](https://github.com/muuki88))

## [v1.6.0](https://github.com/sbt/sbt-native-packager/tree/v1.6.0) (2020-01-18)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.5.2...v1.6.0)

**Closed issues:**

- jlinkModules fails for modules that require automatic modules [\#1293](https://github.com/sbt/sbt-native-packager/issues/1293)

**Merged pull requests:**

- Replace dockerVersion with dockerApiVersion [\#1296](https://github.com/sbt/sbt-native-packager/pull/1296) ([rodrigorn](https://github.com/rodrigorn))
- JlinkPlugin: Add and document a workaround for automatic modules [\#1295](https://github.com/sbt/sbt-native-packager/pull/1295) ([nigredo-tori](https://github.com/nigredo-tori))
- Add folders from Bloop and Metals to gitignore [\#1292](https://github.com/sbt/sbt-native-packager/pull/1292) ([jan0sch](https://github.com/jan0sch))
- Remove mailinglist [\#1291](https://github.com/sbt/sbt-native-packager/pull/1291) ([muuki88](https://github.com/muuki88))

## [v1.5.2](https://github.com/sbt/sbt-native-packager/tree/v1.5.2) (2019-12-11)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.5.1...v1.5.2)

**Closed issues:**

- Issue building graalvm-native-image [\#1288](https://github.com/sbt/sbt-native-packager/issues/1288)
- missing files cause 'java.io.IOException: This archive contains unclosed entries.' [\#1035](https://github.com/sbt/sbt-native-packager/issues/1035)

**Merged pull requests:**

- Improve error handling in ZIP utilities [\#1290](https://github.com/sbt/sbt-native-packager/pull/1290) ([retronym](https://github.com/retronym))
- Update outdated links [\#1289](https://github.com/sbt/sbt-native-packager/pull/1289) ([mkurz](https://github.com/mkurz))
- Add link to working example of BuildEnvPlugin [\#1283](https://github.com/sbt/sbt-native-packager/pull/1283) ([ryanberckmans](https://github.com/ryanberckmans))

## [v1.5.1](https://github.com/sbt/sbt-native-packager/tree/v1.5.1) (2019-11-25)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.5.0...v1.5.1)

**Fixed bugs:**

- 1.5.0 regression in JLinkPLugin error handling  [\#1284](https://github.com/sbt/sbt-native-packager/issues/1284)
- Fix 1.5.0 regression in JLinkPLugin error handling [\#1285](https://github.com/sbt/sbt-native-packager/pull/1285) ([nigredo-tori](https://github.com/nigredo-tori))

**Closed issues:**

- WindowsPlugin: support portable WiX [\#1281](https://github.com/sbt/sbt-native-packager/issues/1281)
- Why does it create an \<none\> image? [\#1229](https://github.com/sbt/sbt-native-packager/issues/1229)

**Merged pull requests:**

- WindowsPlugin: add portable WiX support [\#1282](https://github.com/sbt/sbt-native-packager/pull/1282) ([nigredo-tori](https://github.com/nigredo-tori))
- Set homepage setting to GitHub repo [\#1280](https://github.com/sbt/sbt-native-packager/pull/1280) ([fthomas](https://github.com/fthomas))

## [v1.5.0](https://github.com/sbt/sbt-native-packager/tree/v1.5.0) (2019-11-18)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.4.1...v1.5.0)

**Fixed bugs:**

- AshScriptPlugin generates bash scripts for periferal mains [\#1272](https://github.com/sbt/sbt-native-packager/issues/1272)

**Closed issues:**

- Remove intermediate images when using docker multi-stage builds [\#1277](https://github.com/sbt/sbt-native-packager/issues/1277)
- docker packager ignores commands [\#1269](https://github.com/sbt/sbt-native-packager/issues/1269)
- JlinkPlugin: find a workaround for the jdeps command line length limit [\#1266](https://github.com/sbt/sbt-native-packager/issues/1266)
- Prevent pushing no-repository images to dockerhub? [\#1265](https://github.com/sbt/sbt-native-packager/issues/1265)
- Docker image with AshScriptPlugin does not run at all [\#1263](https://github.com/sbt/sbt-native-packager/issues/1263)
- What to do when useradd is not available? [\#1262](https://github.com/sbt/sbt-native-packager/issues/1262)
- Empty dockerExposedPorts should be okay [\#1260](https://github.com/sbt/sbt-native-packager/issues/1260)
- AshScriptPlugin does not add process\_args\(\) to ash-template [\#1254](https://github.com/sbt/sbt-native-packager/issues/1254)
- dockerhub private repo authentication [\#654](https://github.com/sbt/sbt-native-packager/issues/654)

**Merged pull requests:**

- Autoremove multi-stage intermediate image\(s\) [\#1279](https://github.com/sbt/sbt-native-packager/pull/1279) ([mkurz](https://github.com/mkurz))
- Fix -debug option description [\#1278](https://github.com/sbt/sbt-native-packager/pull/1278) ([nigredo-tori](https://github.com/nigredo-tori))
- Mention "username" and "password" for docker registry login [\#1275](https://github.com/sbt/sbt-native-packager/pull/1275) ([mkurz](https://github.com/mkurz))
- umask can also be set via bashScriptExtraDefines [\#1274](https://github.com/sbt/sbt-native-packager/pull/1274) ([mkurz](https://github.com/mkurz))
- Add a workaround for lightbend/mima\#422 [\#1271](https://github.com/sbt/sbt-native-packager/pull/1271) ([nigredo-tori](https://github.com/nigredo-tori))
- JlinkPlugin: add support for huge classpaths [\#1270](https://github.com/sbt/sbt-native-packager/pull/1270) ([nigredo-tori](https://github.com/nigredo-tori))

## [v1.4.1](https://github.com/sbt/sbt-native-packager/tree/v1.4.1) (2019-08-29)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.4.0...v1.4.1)

**Closed issues:**

- Build Graal native images in a docker container [\#1250](https://github.com/sbt/sbt-native-packager/issues/1250)

**Merged pull requests:**

- Added command line JAVA\_OPTS support for AshScriptPlugin [\#1255](https://github.com/sbt/sbt-native-packager/pull/1255) ([farico](https://github.com/farico))

## [v1.4.0](https://github.com/sbt/sbt-native-packager/tree/v1.4.0) (2019-08-21)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.25...v1.4.0)

**Fixed bugs:**

- AshScriptPlugin forbids command parameters [\#1017](https://github.com/sbt/sbt-native-packager/issues/1017)

**Closed issues:**

- JlinkPlugin: properly handle external modules  [\#1247](https://github.com/sbt/sbt-native-packager/issues/1247)
- Flaky rpm tests [\#1246](https://github.com/sbt/sbt-native-packager/issues/1246)
- fileDescriptorLimit could not set according to system default value [\#1158](https://github.com/sbt/sbt-native-packager/issues/1158)
- Can sbt-native-packager help to create Release files? [\#1129](https://github.com/sbt/sbt-native-packager/issues/1129)
- Make systemd service option available to universal tarball. [\#1115](https://github.com/sbt/sbt-native-packager/issues/1115)
- java.lang.ArrayIndexOutOfBoundsException: 1 when running sbt elastic-beanstalk:dist [\#1098](https://github.com/sbt/sbt-native-packager/issues/1098)
- Support multiple docker aliases [\#1081](https://github.com/sbt/sbt-native-packager/issues/1081)
- Multiple issues building debian for Systemd from play application [\#1050](https://github.com/sbt/sbt-native-packager/issues/1050)
- \[Idea\] JDK 9's jlink & sbt-native-packager? [\#1043](https://github.com/sbt/sbt-native-packager/issues/1043)
- Systemd packaging doesn't support multiple `EnvironmentFile` options [\#968](https://github.com/sbt/sbt-native-packager/issues/968)
- Mappings are not contained in JDKPackager bundles [\#782](https://github.com/sbt/sbt-native-packager/issues/782)
- option to exclude scala-library explicitly  [\#716](https://github.com/sbt/sbt-native-packager/issues/716)
- Create integration tests [\#545](https://github.com/sbt/sbt-native-packager/issues/545)
- Feature request: createHomeDirectory [\#238](https://github.com/sbt/sbt-native-packager/issues/238)
- windows installer [\#12](https://github.com/sbt/sbt-native-packager/issues/12)

**Merged pull requests:**

- Support building Graal native images in docker [\#1251](https://github.com/sbt/sbt-native-packager/pull/1251) ([jroper](https://github.com/jroper))
- Fox \#1246 flaky rpm tests [\#1249](https://github.com/sbt/sbt-native-packager/pull/1249) ([muuki88](https://github.com/muuki88))
- JlinkPlugin: restrict linking to platform modules [\#1248](https://github.com/sbt/sbt-native-packager/pull/1248) ([nigredo-tori](https://github.com/nigredo-tori))

## [v1.3.25](https://github.com/sbt/sbt-native-packager/tree/v1.3.25) (2019-07-10)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.24...v1.3.25)

**Closed issues:**

- JlinkPlugin: support multi-release dependencies [\#1243](https://github.com/sbt/sbt-native-packager/issues/1243)
- Assorted JlinkPlugin improvements [\#1240](https://github.com/sbt/sbt-native-packager/issues/1240)

**Merged pull requests:**

- JlinkPlugin: Sort missing dependencies before logging [\#1245](https://github.com/sbt/sbt-native-packager/pull/1245) ([nigredo-tori](https://github.com/nigredo-tori))
- JlinkPlugin: support multi-release dependencies [\#1244](https://github.com/sbt/sbt-native-packager/pull/1244) ([nigredo-tori](https://github.com/nigredo-tori))

## [v1.3.24](https://github.com/sbt/sbt-native-packager/tree/v1.3.24) (2019-06-26)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.23...v1.3.24)

**Merged pull requests:**

- Assorted JlinkPlugin improvements [\#1242](https://github.com/sbt/sbt-native-packager/pull/1242) ([nigredo-tori](https://github.com/nigredo-tori))

## [v1.3.23](https://github.com/sbt/sbt-native-packager/tree/v1.3.23) (2019-06-24)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.22...v1.3.23)

**Closed issues:**

- Swtich to OpenJDK [\#1239](https://github.com/sbt/sbt-native-packager/issues/1239)
- Deployed documentation is outdated [\#1236](https://github.com/sbt/sbt-native-packager/issues/1236)
- start/stop lifecycle of application with sbt-native-packager [\#1232](https://github.com/sbt/sbt-native-packager/issues/1232)
- how to question :: integration with gitlab CI [\#1213](https://github.com/sbt/sbt-native-packager/issues/1213)
- Problem with docker:publishLocal for alpine [\#1202](https://github.com/sbt/sbt-native-packager/issues/1202)

**Merged pull requests:**

- Use OpenJDK intead of Oracle JDK [\#1241](https://github.com/sbt/sbt-native-packager/pull/1241) ([jiminhsieh](https://github.com/jiminhsieh))
- Reuse cache for GraalVM and sbt [\#1237](https://github.com/sbt/sbt-native-packager/pull/1237) ([jiminhsieh](https://github.com/jiminhsieh))
- Possible improvement for \#1202 [\#1235](https://github.com/sbt/sbt-native-packager/pull/1235) ([borice](https://github.com/borice))

## [v1.3.22](https://github.com/sbt/sbt-native-packager/tree/v1.3.22) (2019-05-28)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.21...v1.3.22)

**Fixed bugs:**

- Docker version detection fails on Ubuntu 16.04 [\#1230](https://github.com/sbt/sbt-native-packager/issues/1230)

**Merged pull requests:**

- Improve Docker version detection [\#1231](https://github.com/sbt/sbt-native-packager/pull/1231) ([eed3si9n](https://github.com/eed3si9n))
- sbt-conductr is EOL [\#1228](https://github.com/sbt/sbt-native-packager/pull/1228) ([wsargent](https://github.com/wsargent))
- remove docker-containers [\#1227](https://github.com/sbt/sbt-native-packager/pull/1227) ([wsargent](https://github.com/wsargent))
- Fix dependency handling in JlinkPlugin \(+ general improvements\) [\#1226](https://github.com/sbt/sbt-native-packager/pull/1226) ([nigredo-tori](https://github.com/nigredo-tori))
- Update graalvm installation script [\#1224](https://github.com/sbt/sbt-native-packager/pull/1224) ([gurinderu](https://github.com/gurinderu))
- fix error message for jdkpackager when antTaskLib is missing [\#1222](https://github.com/sbt/sbt-native-packager/pull/1222) ([Sciss](https://github.com/Sciss))
- Mention discoveredMainClasses in documentation [\#1216](https://github.com/sbt/sbt-native-packager/pull/1216) ([Discipe](https://github.com/Discipe))

## [v1.3.21](https://github.com/sbt/sbt-native-packager/tree/v1.3.21) (2019-05-05)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.20...v1.3.21)

**Fixed bugs:**

- docker:publish fails if the given daemonGroupGid does not exists [\#1218](https://github.com/sbt/sbt-native-packager/issues/1218)

**Closed issues:**

- Idea: package an app for homebrew? [\#1210](https://github.com/sbt/sbt-native-packager/issues/1210)

**Merged pull requests:**

- Add a simple jlink wrapper [\#1220](https://github.com/sbt/sbt-native-packager/pull/1220) ([nigredo-tori](https://github.com/nigredo-tori))
- Bug \#1218: docker:publishLocal fails because of non-existent gid [\#1219](https://github.com/sbt/sbt-native-packager/pull/1219) ([NicolasRouquette](https://github.com/NicolasRouquette))
- correct sbt console example [\#1214](https://github.com/sbt/sbt-native-packager/pull/1214) ([mcanlas](https://github.com/mcanlas))
- Log rpm output to error or info depending on exit code [\#1212](https://github.com/sbt/sbt-native-packager/pull/1212) ([Falmarri](https://github.com/Falmarri))

## [v1.3.20](https://github.com/sbt/sbt-native-packager/tree/v1.3.20) (2019-03-29)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.19...v1.3.20)

**Merged pull requests:**

- Don't write RPM standard output as error [\#1211](https://github.com/sbt/sbt-native-packager/pull/1211) ([Falmarri](https://github.com/Falmarri))
- Update Proguard example [\#1209](https://github.com/sbt/sbt-native-packager/pull/1209) ([ipostanogov](https://github.com/ipostanogov))

## [v1.3.19](https://github.com/sbt/sbt-native-packager/tree/v1.3.19) (2019-03-02)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.18...v1.3.19)

**Fixed bugs:**

- Error "Reference to undefined setting: makeBashScripts from dockerAdditionalPermissions" \(since v1.3.18\) [\#1205](https://github.com/sbt/sbt-native-packager/issues/1205)
- Docs for specific version not published \(regression\) [\#1203](https://github.com/sbt/sbt-native-packager/issues/1203)

**Closed issues:**

- javaOptions in Universal doesn't have effect in custom plugin [\#1208](https://github.com/sbt/sbt-native-packager/issues/1208)
- Is there a way to skip tests? [\#1204](https://github.com/sbt/sbt-native-packager/issues/1204)
- Docker/stage generates different Dockerfile depending on docker version [\#1187](https://github.com/sbt/sbt-native-packager/issues/1187)

**Merged pull requests:**

- FIX \#1205 undefined setting makeBashScripts with vanilla DockerPlugin [\#1207](https://github.com/sbt/sbt-native-packager/pull/1207) ([muuki88](https://github.com/muuki88))

## [v1.3.18](https://github.com/sbt/sbt-native-packager/tree/v1.3.18) (2019-02-12)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.17...v1.3.18)

**Fixed bugs:**

- Dockerfile switches to daemon user uid instead of the daemonUser [\#1198](https://github.com/sbt/sbt-native-packager/issues/1198)

**Closed issues:**

- Defining a user in multi-stage builds [\#1195](https://github.com/sbt/sbt-native-packager/issues/1195)
- Found conflicts in dependencies [\#1153](https://github.com/sbt/sbt-native-packager/issues/1153)

**Merged pull requests:**

- Run chmod +x explicitly in stage0 [\#1201](https://github.com/sbt/sbt-native-packager/pull/1201) ([eed3si9n](https://github.com/eed3si9n))
- Use daemonUserUid to opt-out of numeric USER [\#1200](https://github.com/sbt/sbt-native-packager/pull/1200) ([eed3si9n](https://github.com/eed3si9n))

## [v1.3.17](https://github.com/sbt/sbt-native-packager/tree/v1.3.17) (2019-01-28)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.16...v1.3.17)

**Closed issues:**

- Put Spotify docker-client back in provided scope [\#1194](https://github.com/sbt/sbt-native-packager/issues/1194)
- OpenShift compatibility [\#1189](https://github.com/sbt/sbt-native-packager/issues/1189)

**Merged pull requests:**

- Run stage0 as root during Docker build [\#1197](https://github.com/sbt/sbt-native-packager/pull/1197) ([eed3si9n](https://github.com/eed3si9n))
- Attempt to move docker-client dependency back to the provided scope [\#1196](https://github.com/sbt/sbt-native-packager/pull/1196) ([gpgekko](https://github.com/gpgekko))
- Update Docker plugin docs [\#1193](https://github.com/sbt/sbt-native-packager/pull/1193) ([eed3si9n](https://github.com/eed3si9n))

## [v1.3.16](https://github.com/sbt/sbt-native-packager/tree/v1.3.16) (2019-01-24)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.15...v1.3.16)

**Closed issues:**

- Add MiMa to check binary compatibility [\#1185](https://github.com/sbt/sbt-native-packager/issues/1185)
- 1.6.0 not compatible with Lagom 1.4.10 [\#1184](https://github.com/sbt/sbt-native-packager/issues/1184)
- LinuxPackageMappings in Rpm. Cannot resolve symbol packageMapping for sbt 0.13.15 [\#1180](https://github.com/sbt/sbt-native-packager/issues/1180)
- Can't specify Epoch for the RPM Plugin [\#1178](https://github.com/sbt/sbt-native-packager/issues/1178)
- WindowsPlugin: support multiple .wxs inputs [\#1176](https://github.com/sbt/sbt-native-packager/issues/1176)

**Merged pull requests:**

- Implement dockerPermissionStrategy [\#1190](https://github.com/sbt/sbt-native-packager/pull/1190) ([eed3si9n](https://github.com/eed3si9n))
- Fix \#1185 Add MiMa to check binary compatibility [\#1188](https://github.com/sbt/sbt-native-packager/pull/1188) ([muuki88](https://github.com/muuki88))
- Fix Codacy's badge link [\#1183](https://github.com/sbt/sbt-native-packager/pull/1183) ([prendi](https://github.com/prendi))
-  \#1178 attempt at adding in rpm epochs [\#1179](https://github.com/sbt/sbt-native-packager/pull/1179) ([Ophirr33](https://github.com/Ophirr33))

## [v1.3.15](https://github.com/sbt/sbt-native-packager/tree/v1.3.15) (2018-11-29)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.14...v1.3.15)

**Merged pull requests:**

- WindowsPlugin: support multiple wxs sources \(\#1176\) [\#1177](https://github.com/sbt/sbt-native-packager/pull/1177) ([nigredo-tori](https://github.com/nigredo-tori))

## [v1.3.14](https://github.com/sbt/sbt-native-packager/tree/v1.3.14) (2018-11-21)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.12...v1.3.14)

**Closed issues:**

- docker:publishLocal publish to private remote repository [\#1174](https://github.com/sbt/sbt-native-packager/issues/1174)

**Merged pull requests:**

- Update scaladoc in contentOf to match method signature [\#1172](https://github.com/sbt/sbt-native-packager/pull/1172) ([frosforever](https://github.com/frosforever))
- Update dependencies [\#1170](https://github.com/sbt/sbt-native-packager/pull/1170) ([marcospereira](https://github.com/marcospereira))

## [v1.3.12](https://github.com/sbt/sbt-native-packager/tree/v1.3.12) (2018-10-27)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.11...v1.3.12)

**Closed issues:**

- Manage graalvm native images [\#1123](https://github.com/sbt/sbt-native-packager/issues/1123)

**Merged pull requests:**

- GraalVM docs to use the installer from master branch [\#1169](https://github.com/sbt/sbt-native-packager/pull/1169) ([ScalaWilliam](https://github.com/ScalaWilliam))
- Improve the GraalVM native-image documentation [\#1168](https://github.com/sbt/sbt-native-packager/pull/1168) ([ScalaWilliam](https://github.com/ScalaWilliam))
- fix typo in README.md [\#1167](https://github.com/sbt/sbt-native-packager/pull/1167) ([hepin1989](https://github.com/hepin1989))
- Add Travis tests for the GraalVL native-image plug-in [\#1166](https://github.com/sbt/sbt-native-packager/pull/1166) ([ScalaWilliam](https://github.com/ScalaWilliam))

## [v1.3.11](https://github.com/sbt/sbt-native-packager/tree/v1.3.11) (2018-10-21)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.10...v1.3.11)

**Fixed bugs:**

- Put jdeb back in provided scope [\#1097](https://github.com/sbt/sbt-native-packager/issues/1097)

**Closed issues:**

- docker: application.ini is not used with ash [\#1162](https://github.com/sbt/sbt-native-packager/issues/1162)
- Cannot append classifier to the tgz generated by Universal [\#1160](https://github.com/sbt/sbt-native-packager/issues/1160)

**Merged pull requests:**

- Build native images using GraalVM [\#1165](https://github.com/sbt/sbt-native-packager/pull/1165) ([ScalaWilliam](https://github.com/ScalaWilliam))
- sbt 1.2.6 [\#1163](https://github.com/sbt/sbt-native-packager/pull/1163) ([sullis](https://github.com/sullis))

## [v1.3.10](https://github.com/sbt/sbt-native-packager/tree/v1.3.10) (2018-09-26)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.9...v1.3.10)

**Closed issues:**

- Docker - Only publish "latest" [\#1159](https://github.com/sbt/sbt-native-packager/issues/1159)
- I cannot use sbt-native-packager to build a setup for test classes [\#1157](https://github.com/sbt/sbt-native-packager/issues/1157)

**Merged pull requests:**

- FIX \#1097 Put jdeb back in provided scope [\#1161](https://github.com/sbt/sbt-native-packager/pull/1161) ([muuki88](https://github.com/muuki88))

## [v1.3.9](https://github.com/sbt/sbt-native-packager/tree/v1.3.9) (2018-09-11)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.8-M15...v1.3.9)

**Closed issues:**

- Automatic relaese process from travis [\#1148](https://github.com/sbt/sbt-native-packager/issues/1148)
- Add validatePackageConfiguration task [\#1026](https://github.com/sbt/sbt-native-packager/issues/1026)

**Merged pull requests:**

- Add kill timeout to SystemD service as TimeoutStopSec [\#1156](https://github.com/sbt/sbt-native-packager/pull/1156) ([andrewgee](https://github.com/andrewgee))
- Wip/1026 validate task [\#1124](https://github.com/sbt/sbt-native-packager/pull/1124) ([muuki88](https://github.com/muuki88))

## [v1.3.8-M15](https://github.com/sbt/sbt-native-packager/tree/v1.3.8-M15) (2018-09-09)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.8-M14...v1.3.8-M15)

## [v1.3.8-M14](https://github.com/sbt/sbt-native-packager/tree/v1.3.8-M14) (2018-09-09)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.7...v1.3.8-M14)

**Fixed bugs:**

- Debian plugin creates incorrect chown command [\#1151](https://github.com/sbt/sbt-native-packager/issues/1151)

**Closed issues:**

- Sed command error in ash-template file [\#1154](https://github.com/sbt/sbt-native-packager/issues/1154)

**Merged pull requests:**

- Fixes \#1154 [\#1155](https://github.com/sbt/sbt-native-packager/pull/1155) ([glammers1](https://github.com/glammers1))

## [v1.3.7](https://github.com/sbt/sbt-native-packager/tree/v1.3.7) (2018-08-31)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.8...v1.3.7)

## [v1.3.8](https://github.com/sbt/sbt-native-packager/tree/v1.3.8) (2018-08-28)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.7-M6...v1.3.8)

**Fixed bugs:**

- docker:publish should skip non-Docker projects [\#974](https://github.com/sbt/sbt-native-packager/issues/974)

**Closed issues:**

- lib directory remains after RPM erase [\#623](https://github.com/sbt/sbt-native-packager/issues/623)
- bin access to script replacements [\#622](https://github.com/sbt/sbt-native-packager/issues/622)
- packageName added to defaultLinuxLogsLocation [\#620](https://github.com/sbt/sbt-native-packager/issues/620)

**Merged pull requests:**

- Fix generated chown command \(\#1151\) [\#1152](https://github.com/sbt/sbt-native-packager/pull/1152) ([mcenkar](https://github.com/mcenkar))

## [v1.3.7-M6](https://github.com/sbt/sbt-native-packager/tree/v1.3.7-M6) (2018-08-27)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.7-M5...v1.3.7-M6)

## [v1.3.7-M5](https://github.com/sbt/sbt-native-packager/tree/v1.3.7-M5) (2018-08-26)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.6...v1.3.7-M5)

**Fixed bugs:**

- dpkg error: archive has premature member [\#1067](https://github.com/sbt/sbt-native-packager/issues/1067)

**Closed issues:**

- Default dockerBaseImage value should be set to Java 8. [\#1146](https://github.com/sbt/sbt-native-packager/issues/1146)
- Version check doesn't work for openjdk 10 [\#1145](https://github.com/sbt/sbt-native-packager/issues/1145)
- Rpm: publish [\#1143](https://github.com/sbt/sbt-native-packager/issues/1143)
- Can you add optional ability of create\delete user\user group [\#1140](https://github.com/sbt/sbt-native-packager/issues/1140)
- Packaged jars are not the same in different \*:packageBin configs [\#1130](https://github.com/sbt/sbt-native-packager/issues/1130)
- Scope of daemon user [\#551](https://github.com/sbt/sbt-native-packager/issues/551)
- Option to use Java libraries instead of native tools when possible [\#422](https://github.com/sbt/sbt-native-packager/issues/422)

**Merged pull requests:**

- Use travis jobs to structure build and deployment [\#1150](https://github.com/sbt/sbt-native-packager/pull/1150) ([muuki88](https://github.com/muuki88))
- FIX \#1148 travis release [\#1149](https://github.com/sbt/sbt-native-packager/pull/1149) ([muuki88](https://github.com/muuki88))
- Change openjdk:latest to openjdk:8. [\#1147](https://github.com/sbt/sbt-native-packager/pull/1147) ([wjlow](https://github.com/wjlow))
- Fix docker login link in the documentation [\#1139](https://github.com/sbt/sbt-native-packager/pull/1139) ([BenFradet](https://github.com/BenFradet))

## [v1.3.6](https://github.com/sbt/sbt-native-packager/tree/v1.3.6) (2018-07-19)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.5...v1.3.6)

**Closed issues:**

- can sbt-native-packager be used to pack /test ? [\#1136](https://github.com/sbt/sbt-native-packager/issues/1136)
- Changes to application.conf are not picked up on Debian [\#1135](https://github.com/sbt/sbt-native-packager/issues/1135)

**Merged pull requests:**

- \[Docker\] add multiple docker tags support [\#1138](https://github.com/sbt/sbt-native-packager/pull/1138) ([kimxogus](https://github.com/kimxogus))
- \[Docker\] add dockerEnvVars support [\#1137](https://github.com/sbt/sbt-native-packager/pull/1137) ([kimxogus](https://github.com/kimxogus))

## [v1.3.5](https://github.com/sbt/sbt-native-packager/tree/v1.3.5) (2018-06-12)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.4...v1.3.5)

**Fixed bugs:**

- Auto generated scripts drop last character if its an `r`  [\#1121](https://github.com/sbt/sbt-native-packager/issues/1121)
- Error while `sbt debian:packageBin` [\#479](https://github.com/sbt/sbt-native-packager/issues/479)

**Closed issues:**

- how to package CLI app on MacOS? [\#1133](https://github.com/sbt/sbt-native-packager/issues/1133)
- Java 10 breaks Unix launcher script [\#1128](https://github.com/sbt/sbt-native-packager/issues/1128)
- systemd vendor preset: enabled - can I change this [\#1127](https://github.com/sbt/sbt-native-packager/issues/1127)
- Custom name for tgz archive [\#1120](https://github.com/sbt/sbt-native-packager/issues/1120)
- sbt docker:stage fails but sbt docker:publishLocal works [\#1006](https://github.com/sbt/sbt-native-packager/issues/1006)
- Support for rocket / rkt [\#557](https://github.com/sbt/sbt-native-packager/issues/557)

**Merged pull requests:**

- sbt 1.1.6 [\#1132](https://github.com/sbt/sbt-native-packager/pull/1132) ([sullis](https://github.com/sullis))
- fix procedure syntax [\#1131](https://github.com/sbt/sbt-native-packager/pull/1131) ([xuwei-k](https://github.com/xuwei-k))
- More grammar changes in docs [\#1126](https://github.com/sbt/sbt-native-packager/pull/1126) ([anilkumarmyla](https://github.com/anilkumarmyla))
- Correct some typos and grammar [\#1125](https://github.com/sbt/sbt-native-packager/pull/1125) ([anilkumarmyla](https://github.com/anilkumarmyla))
- fixes \#1121  [\#1122](https://github.com/sbt/sbt-native-packager/pull/1122) ([tobyweston](https://github.com/tobyweston))
- Fix typo in TaskKey for rpmLint. [\#1119](https://github.com/sbt/sbt-native-packager/pull/1119) ([daanhoogenboezem](https://github.com/daanhoogenboezem))

## [v1.3.4](https://github.com/sbt/sbt-native-packager/tree/v1.3.4) (2018-04-06)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.3...v1.3.4)

**Fixed bugs:**

- Ability to independently override different systemloader templates [\#937](https://github.com/sbt/sbt-native-packager/issues/937)

**Closed issues:**

- Remove requirement of JvmPlugin from UniversalPlugin [\#1116](https://github.com/sbt/sbt-native-packager/issues/1116)
- Feature request - publishM2 [\#1114](https://github.com/sbt/sbt-native-packager/issues/1114)
- Deb package dependencies [\#1106](https://github.com/sbt/sbt-native-packager/issues/1106)
- unresolved dependency: com.typesafe.sbt\#sbt-native-packager;1.1.4: not found [\#1100](https://github.com/sbt/sbt-native-packager/issues/1100)

**Merged pull requests:**

- Included feature publishM2 [\#1113](https://github.com/sbt/sbt-native-packager/pull/1113) ([giorgioinf](https://github.com/giorgioinf))
- Process -XX arguments as JVM args [\#1112](https://github.com/sbt/sbt-native-packager/pull/1112) ([longshorej](https://github.com/longshorej))
- fixed Java version check in bash template [\#1111](https://github.com/sbt/sbt-native-packager/pull/1111) ([jubecker](https://github.com/jubecker))
- Fix docker server version parsing [\#1108](https://github.com/sbt/sbt-native-packager/pull/1108) ([jalaziz](https://github.com/jalaziz))
- JRE dependencies in Debian documentation [\#1107](https://github.com/sbt/sbt-native-packager/pull/1107) ([GreyCat](https://github.com/GreyCat))
- Upgrade Scala version [\#1103](https://github.com/sbt/sbt-native-packager/pull/1103) ([joan38](https://github.com/joan38))
- SBT 1.1.1 [\#1102](https://github.com/sbt/sbt-native-packager/pull/1102) ([joan38](https://github.com/joan38))
- Give an absolute path to the entrypoint [\#1101](https://github.com/sbt/sbt-native-packager/pull/1101) ([joan38](https://github.com/joan38))
- Typo: dpgk -\> dpkg [\#1099](https://github.com/sbt/sbt-native-packager/pull/1099) ([raboof](https://github.com/raboof))

## [v1.3.3](https://github.com/sbt/sbt-native-packager/tree/v1.3.3) (2018-02-03)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.2...v1.3.3)

**Fixed bugs:**

- Break arguments with spaces in ash [\#1094](https://github.com/sbt/sbt-native-packager/issues/1094)
- Documentation for specific version not published anymore [\#1087](https://github.com/sbt/sbt-native-packager/issues/1087)
- Remove carriage return characters when loading configuration files. [\#1074](https://github.com/sbt/sbt-native-packager/issues/1074)
- java.lang.NoClassDefFoundError: org/vafer/jdeb/Console [\#1053](https://github.com/sbt/sbt-native-packager/issues/1053)
- Duplicated startup script names in universal zip [\#1016](https://github.com/sbt/sbt-native-packager/issues/1016)

**Closed issues:**

- bashScriptConfigLocation config  invalid [\#1076](https://github.com/sbt/sbt-native-packager/issues/1076)
- Don't compress packages using the JDeb debian packager [\#1072](https://github.com/sbt/sbt-native-packager/issues/1072)
- APP\_CLASSPATH line growths too long in windows startup batch script [\#1071](https://github.com/sbt/sbt-native-packager/issues/1071)
- Docker Errors on Load [\#1065](https://github.com/sbt/sbt-native-packager/issues/1065)
- Doc and src packages for 1.3.2 not found in repo1.maven.org [\#1063](https://github.com/sbt/sbt-native-packager/issues/1063)
- \[Feature Request\] Universal plugin, directory output format [\#1061](https://github.com/sbt/sbt-native-packager/issues/1061)
- Remove redundant docker file [\#1060](https://github.com/sbt/sbt-native-packager/issues/1060)
- Upstart init script does not source environment variables, nor does it pass environment variables on [\#1023](https://github.com/sbt/sbt-native-packager/issues/1023)
- SystemV service template does not actually generate any logs [\#1021](https://github.com/sbt/sbt-native-packager/issues/1021)
- Docs do not accurately describe how to install plugin [\#797](https://github.com/sbt/sbt-native-packager/issues/797)
- Adding a Code of Conduct [\#744](https://github.com/sbt/sbt-native-packager/issues/744)

**Merged pull requests:**

- Fix systemv logging [\#1096](https://github.com/sbt/sbt-native-packager/pull/1096) ([kardapoltsev](https://github.com/kardapoltsev))
- do not break arguments with spaces inside [\#1095](https://github.com/sbt/sbt-native-packager/pull/1095) ([yanns](https://github.com/yanns))
- DockerAlias should use dockerRepository and dockerUsername from Docker scope [\#1092](https://github.com/sbt/sbt-native-packager/pull/1092) ([jjst](https://github.com/jjst))
- Fix typo in docker.rst [\#1091](https://github.com/sbt/sbt-native-packager/pull/1091) ([mattinbits](https://github.com/mattinbits))
- Fix typo in src/sphinx/formats/docker.rst [\#1089](https://github.com/sbt/sbt-native-packager/pull/1089) ([kasonchan](https://github.com/kasonchan))
- fix packageZipTarball defaults for UniversalDocs and UniversalSource [\#1086](https://github.com/sbt/sbt-native-packager/pull/1086) ([muuki88](https://github.com/muuki88))
- Update readme during relaese [\#1084](https://github.com/sbt/sbt-native-packager/pull/1084) ([muuki88](https://github.com/muuki88))
- Add scoped resolvers to otherResolvers [\#1083](https://github.com/sbt/sbt-native-packager/pull/1083) ([muuki88](https://github.com/muuki88))
- FIX \#744 Add code of conduct [\#1082](https://github.com/sbt/sbt-native-packager/pull/1082) ([muuki88](https://github.com/muuki88))
- Enable environment export for upstart loader [\#1080](https://github.com/sbt/sbt-native-packager/pull/1080) ([kardapoltsev](https://github.com/kardapoltsev))
- Remove carriage return characters when loading configuration files. [\#1078](https://github.com/sbt/sbt-native-packager/pull/1078) ([JustinPihony](https://github.com/JustinPihony))
- Fix \#1076 add more documentation on bash and bat script configuration [\#1077](https://github.com/sbt/sbt-native-packager/pull/1077) ([muuki88](https://github.com/muuki88))
- Update release version in download instructions [\#1075](https://github.com/sbt/sbt-native-packager/pull/1075) ([agemooij](https://github.com/agemooij))
- Don't compress packages using the JDeb debian packager [\#1073](https://github.com/sbt/sbt-native-packager/pull/1073) ([ajrnz](https://github.com/ajrnz))
- Remove redundant Dockerfile [\#1062](https://github.com/sbt/sbt-native-packager/pull/1062) ([mrfyda](https://github.com/mrfyda))
- Change startup script name generation [\#1020](https://github.com/sbt/sbt-native-packager/pull/1020) ([atrosinenko](https://github.com/atrosinenko))

## [v1.3.2](https://github.com/sbt/sbt-native-packager/tree/v1.3.2) (2017-11-01)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.1...v1.3.2)

**Fixed bugs:**

- when project version is a SNAPSHOT, `rpm:packageBin` fails with "`version in Rpm` is empty." [\#1004](https://github.com/sbt/sbt-native-packager/issues/1004)

**Closed issues:**

- question: are jar files compressed? [\#1046](https://github.com/sbt/sbt-native-packager/issues/1046)
- Docker Image 'chown -R daemon:daemon' duplicates application layer [\#883](https://github.com/sbt/sbt-native-packager/issues/883)
- systemd service customization \(open files\) [\#728](https://github.com/sbt/sbt-native-packager/issues/728)
- Play pid is a big caveat for people starting using sbt-native-packager. [\#602](https://github.com/sbt/sbt-native-packager/issues/602)
- Evalualte circleci.com for docker intergation tests [\#497](https://github.com/sbt/sbt-native-packager/issues/497)

**Merged pull requests:**

- Run docker tests on travis [\#1059](https://github.com/sbt/sbt-native-packager/pull/1059) ([muuki88](https://github.com/muuki88))
- Convert dockerVersion from a setting to a task [\#1058](https://github.com/sbt/sbt-native-packager/pull/1058) ([dwickern](https://github.com/dwickern))
- Update README.md [\#1057](https://github.com/sbt/sbt-native-packager/pull/1057) ([gurghet](https://github.com/gurghet))
- Fix duplicate application.ini mappings when both BASH and BAT plugins are used [\#1056](https://github.com/sbt/sbt-native-packager/pull/1056) ([dwickern](https://github.com/dwickern))
- Improve main class detection [\#1055](https://github.com/sbt/sbt-native-packager/pull/1055) ([dwickern](https://github.com/dwickern))

## [v1.3.1](https://github.com/sbt/sbt-native-packager/tree/v1.3.1) (2017-10-24)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.3.0...v1.3.1)

**Closed issues:**

- Error running rpm:packageBin [\#1054](https://github.com/sbt/sbt-native-packager/issues/1054)
- After upgrading to version 1.3.0, sbt started throwing "docker: 'version --format '{{.Server.Version}}'' not a docker command" warnings on console [\#1051](https://github.com/sbt/sbt-native-packager/issues/1051)

**Merged pull requests:**

- Fix dockerVersion command creation [\#1052](https://github.com/sbt/sbt-native-packager/pull/1052) ([mrfyda](https://github.com/mrfyda))

## [v1.3.0](https://github.com/sbt/sbt-native-packager/tree/v1.3.0) (2017-10-23)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.2.2...v1.3.0)

**Closed issues:**

- question: how to disable universal packager script creation [\#1049](https://github.com/sbt/sbt-native-packager/issues/1049)
- question: How do I pass JVM option to container ? [\#1047](https://github.com/sbt/sbt-native-packager/issues/1047)
- Support SBT 1.0 [\#1041](https://github.com/sbt/sbt-native-packager/issues/1041)
- MAINTAINER in Dockerfile [\#1033](https://github.com/sbt/sbt-native-packager/issues/1033)
- Provide means to set umask of user? [\#1032](https://github.com/sbt/sbt-native-packager/issues/1032)
- \[1.2.2\] docker:publishLocal only succeeds with JavaAppPackaging plugin but not with DockerPlugin [\#1030](https://github.com/sbt/sbt-native-packager/issues/1030)
- Add support for --chown flag for ADD/COPY Docker commands [\#1029](https://github.com/sbt/sbt-native-packager/issues/1029)
- Pass proxy configuration to the application in startup script [\#1015](https://github.com/sbt/sbt-native-packager/issues/1015)
- Publish build for sbt 1.0.0 [\#815](https://github.com/sbt/sbt-native-packager/issues/815)
- Universal java options ignored on Windows - cannot set javaOptions [\#688](https://github.com/sbt/sbt-native-packager/issues/688)
- use case: single project, multiple apps [\#633](https://github.com/sbt/sbt-native-packager/issues/633)

**Merged pull requests:**

- Unset JAVA\_OPTIONS in travis build [\#1048](https://github.com/sbt/sbt-native-packager/pull/1048) ([muuki88](https://github.com/muuki88))
- Add support for --chown flag for ADD/COPY Docker commands [\#1044](https://github.com/sbt/sbt-native-packager/pull/1044) ([mrfyda](https://github.com/mrfyda))
- Windows batch script improvements [\#1042](https://github.com/sbt/sbt-native-packager/pull/1042) ([dwickern](https://github.com/dwickern))
- Fix launcher jar paths not being quoted on Windows [\#1040](https://github.com/sbt/sbt-native-packager/pull/1040) ([dwickern](https://github.com/dwickern))
- Refresh PR: Treat symlinks as normal files in spec file [\#1039](https://github.com/sbt/sbt-native-packager/pull/1039) ([erickpintor](https://github.com/erickpintor))
- Add recipe for setting umask [\#1038](https://github.com/sbt/sbt-native-packager/pull/1038) ([keirlawson](https://github.com/keirlawson))
- Fix typo in universal.rst [\#1037](https://github.com/sbt/sbt-native-packager/pull/1037) ([srinchiera](https://github.com/srinchiera))
- \[docs\] fix typo [\#1036](https://github.com/sbt/sbt-native-packager/pull/1036) ([dwickern](https://github.com/dwickern))
- Use LABEL instead of MAINTAINER for maintainers [\#1034](https://github.com/sbt/sbt-native-packager/pull/1034) ([NeQuissimus](https://github.com/NeQuissimus))
- add available main classes to the usage notes in bash script [\#1027](https://github.com/sbt/sbt-native-packager/pull/1027) ([frosforever](https://github.com/frosforever))
- Fix inconsistency in docs [\#1025](https://github.com/sbt/sbt-native-packager/pull/1025) ([Discipe](https://github.com/Discipe))
- Wip/build with sbt 1.0 [\#1013](https://github.com/sbt/sbt-native-packager/pull/1013) ([muuki88](https://github.com/muuki88))

## [v1.2.2](https://github.com/sbt/sbt-native-packager/tree/v1.2.2) (2017-08-21)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.2.2-RC2...v1.2.2)

**Merged pull requests:**

- Wrap docker labels with " character [\#1014](https://github.com/sbt/sbt-native-packager/pull/1014) ([kimxogus](https://github.com/kimxogus))
- Dependency updates: [\#1012](https://github.com/sbt/sbt-native-packager/pull/1012) ([golem131](https://github.com/golem131))
- Cross build with sbt 1.0 [\#1000](https://github.com/sbt/sbt-native-packager/pull/1000) ([muuki88](https://github.com/muuki88))

## [v1.2.2-RC2](https://github.com/sbt/sbt-native-packager/tree/v1.2.2-RC2) (2017-08-07)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.2.1...v1.2.2-RC2)

## [v1.2.1](https://github.com/sbt/sbt-native-packager/tree/v1.2.1) (2017-08-05)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.2.0...v1.2.1)

**Fixed bugs:**

- restartService error during yum update of RPM package [\#1001](https://github.com/sbt/sbt-native-packager/issues/1001)
- \[Debian\] Broken CHOWN\_REPLACEMENT when built on Windows [\#600](https://github.com/sbt/sbt-native-packager/issues/600)
- JDebPackaging forces .deb dependencies as default [\#567](https://github.com/sbt/sbt-native-packager/issues/567)

**Closed issues:**

- Recipe for including logging config or similar [\#1010](https://github.com/sbt/sbt-native-packager/issues/1010)
- Add option for appending timestamps to RPM snapshots [\#1008](https://github.com/sbt/sbt-native-packager/issues/1008)
- how to insert a custom command into start script [\#1007](https://github.com/sbt/sbt-native-packager/issues/1007)
- 2.11 and 2.12 have no deploy at bintray [\#1002](https://github.com/sbt/sbt-native-packager/issues/1002)
- Chown: invalid group after setting daemonGroup [\#999](https://github.com/sbt/sbt-native-packager/issues/999)
- rpmBrpJavaRepackJars issue [\#964](https://github.com/sbt/sbt-native-packager/issues/964)
- Does docker:publishLocal support multiple modules? [\#941](https://github.com/sbt/sbt-native-packager/issues/941)

**Merged pull requests:**

- Rpm Metadata Small Typo Fix "vaid" changed back to "valid" [\#1009](https://github.com/sbt/sbt-native-packager/pull/1009) ([ChristopherDavenport](https://github.com/ChristopherDavenport))
- strip snapshot suffix safely \(\#1004\) [\#1005](https://github.com/sbt/sbt-native-packager/pull/1005) ([colin-lamed](https://github.com/colin-lamed))
- Fix a bug which cause upgrade failing because of no restartService [\#1003](https://github.com/sbt/sbt-native-packager/pull/1003) ([buster84](https://github.com/buster84))
- sbt 0.13.15. fix warnings [\#998](https://github.com/sbt/sbt-native-packager/pull/998) ([xuwei-k](https://github.com/xuwei-k))
- update scalatest 3.0.3 [\#997](https://github.com/sbt/sbt-native-packager/pull/997) ([xuwei-k](https://github.com/xuwei-k))
- delete unnecessary file [\#996](https://github.com/sbt/sbt-native-packager/pull/996) ([xuwei-k](https://github.com/xuwei-k))

## [v1.2.0](https://github.com/sbt/sbt-native-packager/tree/v1.2.0) (2017-06-07)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.1.6...v1.2.0)

**Fixed bugs:**

- Packaging deb package with 1.2.0-M9 doesn't generate `conffiles` file [\#984](https://github.com/sbt/sbt-native-packager/issues/984)
- SystemVPlugin on Debian not working [\#955](https://github.com/sbt/sbt-native-packager/issues/955)

**Closed issues:**

- You probably access the destination server through a proxy server that is not well configured. [\#988](https://github.com/sbt/sbt-native-packager/issues/988)
- Add maven-plugin to classpathTypes when using the JDebPlugin [\#982](https://github.com/sbt/sbt-native-packager/issues/982)
- Documentation incorrect -- Docker v1.10 or higher required [\#981](https://github.com/sbt/sbt-native-packager/issues/981)
- Publish Arbitrary Docker Tags [\#979](https://github.com/sbt/sbt-native-packager/issues/979)
- way to skip pom generation? [\#975](https://github.com/sbt/sbt-native-packager/issues/975)
- An equivalent to --no-cache [\#973](https://github.com/sbt/sbt-native-packager/issues/973)
- Override DockerAlias toString method [\#972](https://github.com/sbt/sbt-native-packager/issues/972)
- Support -SNAPSHOT versions when generating RPMs by default [\#971](https://github.com/sbt/sbt-native-packager/issues/971)
- RPM with `killtimeout` [\#957](https://github.com/sbt/sbt-native-packager/issues/957)
- Docker authentication fails with latest milestone [\#892](https://github.com/sbt/sbt-native-packager/issues/892)
- Environment Variables in Command Not Handled [\#868](https://github.com/sbt/sbt-native-packager/issues/868)
- Set entrypoint script exection bit when generating Dockerfile [\#861](https://github.com/sbt/sbt-native-packager/issues/861)
- add rpmExplodedPackage command [\#848](https://github.com/sbt/sbt-native-packager/issues/848)
- Evaluate LGTM service [\#833](https://github.com/sbt/sbt-native-packager/issues/833)
- Java Options with space in them cannot be added [\#681](https://github.com/sbt/sbt-native-packager/issues/681)

**Merged pull requests:**

- Use SNAPSHOT for release when snapshot version [\#995](https://github.com/sbt/sbt-native-packager/pull/995) ([keirlawson](https://github.com/keirlawson))
- Preserve executable permissions when creating a tarball [\#993](https://github.com/sbt/sbt-native-packager/pull/993) ([Krever](https://github.com/Krever))
- Allow specifying classifier for default package in deployment settings [\#992](https://github.com/sbt/sbt-native-packager/pull/992) ([Krever](https://github.com/Krever))
- FIX \#981 Documentation incorrect -- Docker v1.10 or higher required [\#990](https://github.com/sbt/sbt-native-packager/pull/990) ([muuki88](https://github.com/muuki88))
- Implement stage command for rpm [\#989](https://github.com/sbt/sbt-native-packager/pull/989) ([muuki88](https://github.com/muuki88))
- Issue 972 pretty print docker alias [\#987](https://github.com/sbt/sbt-native-packager/pull/987) ([muuki88](https://github.com/muuki88))
- Issue 984 depend on conffiles [\#986](https://github.com/sbt/sbt-native-packager/pull/986) ([muuki88](https://github.com/muuki88))
- Add maven-plugin to classpathTypes when using JDebPlugin [\#985](https://github.com/sbt/sbt-native-packager/pull/985) ([muuki88](https://github.com/muuki88))
- Adjust mixed indenting in start-debian-template. [\#983](https://github.com/sbt/sbt-native-packager/pull/983) ([jan0sch](https://github.com/jan0sch))
- Add Setting for Docker User [\#980](https://github.com/sbt/sbt-native-packager/pull/980) ([apeschel](https://github.com/apeschel))
- Prevent empty double quotes in start daemon line. [\#977](https://github.com/sbt/sbt-native-packager/pull/977) ([jan0sch](https://github.com/jan0sch))
- update README [\#976](https://github.com/sbt/sbt-native-packager/pull/976) ([eduedix](https://github.com/eduedix))
- Improve comments in etc-default files [\#969](https://github.com/sbt/sbt-native-packager/pull/969) ([ennru](https://github.com/ennru))
- FIX \#856 Add documentation for daemonUser/Group settings [\#967](https://github.com/sbt/sbt-native-packager/pull/967) ([muuki88](https://github.com/muuki88))
- Add docker:clean task [\#965](https://github.com/sbt/sbt-native-packager/pull/965) ([NeQuissimus](https://github.com/NeQuissimus))
- Add Docker labels easily [\#962](https://github.com/sbt/sbt-native-packager/pull/962) ([NeQuissimus](https://github.com/NeQuissimus))

## [v1.1.6](https://github.com/sbt/sbt-native-packager/tree/v1.1.6) (2017-04-30)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.2.0-M9...v1.1.6)

**Fixed bugs:**

- Trouble using \*daemonUser\* and \*daemonGroup\* settings for RPM  [\#856](https://github.com/sbt/sbt-native-packager/issues/856)
- application does not receive SIGINT signal when using ash. [\#707](https://github.com/sbt/sbt-native-packager/issues/707)

**Closed issues:**

- Forbidden access on bintray [\#966](https://github.com/sbt/sbt-native-packager/issues/966)
- Versioned docs [\#963](https://github.com/sbt/sbt-native-packager/issues/963)
- docker:publishLocal does not override latest tag [\#857](https://github.com/sbt/sbt-native-packager/issues/857)
- Adding support for docker build args [\#844](https://github.com/sbt/sbt-native-packager/issues/844)
- Clean image after publishing [\#501](https://github.com/sbt/sbt-native-packager/issues/501)

**Merged pull requests:**

- Replace deprecated java Docker container with openjdk \(\#877\) [\#970](https://github.com/sbt/sbt-native-packager/pull/970) ([edouardKaiser](https://github.com/edouardKaiser))

## [v1.2.0-M9](https://github.com/sbt/sbt-native-packager/tree/v1.2.0-M9) (2017-04-12)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.2.0-M8...v1.2.0-M9)

**Fixed bugs:**

- packageName in Docker has no effect [\#947](https://github.com/sbt/sbt-native-packager/issues/947)
- Windows: error CNDL0014 $number is not a legal identifier [\#946](https://github.com/sbt/sbt-native-packager/issues/946)
- DebianDeployPlugin publishes only changes [\#587](https://github.com/sbt/sbt-native-packager/issues/587)

**Closed issues:**

- Multiple rpm/deb packages with package dependency based on project dependency [\#956](https://github.com/sbt/sbt-native-packager/issues/956)
- Add support for HEALTHCHECK in DockerPlugin [\#949](https://github.com/sbt/sbt-native-packager/issues/949)
- `rpm:packageBin` reports "`rpmVendor in Rpm` is empty" despite a value being set [\#943](https://github.com/sbt/sbt-native-packager/issues/943)
- Support JAVA\_OPTS for Ash template, similar to bash [\#940](https://github.com/sbt/sbt-native-packager/issues/940)
- 1.2.0-M8 adds an ADD opt command to the Dockerfile - why? [\#936](https://github.com/sbt/sbt-native-packager/issues/936)
- Image is not tagged when using dockerBuildOptions [\#935](https://github.com/sbt/sbt-native-packager/issues/935)
- How would one use this in a maven project [\#934](https://github.com/sbt/sbt-native-packager/issues/934)
- Don't override packageName in UniversalPlugin [\#902](https://github.com/sbt/sbt-native-packager/issues/902)

**Merged pull requests:**

- Fix DebianDeployPlugin not publishing deb file [\#961](https://github.com/sbt/sbt-native-packager/pull/961) ([eed3si9n](https://github.com/eed3si9n))
- \#957 RPM with `killTimeout` [\#960](https://github.com/sbt/sbt-native-packager/pull/960) ([mr-git](https://github.com/mr-git))
- Fixes handling of directories with numbers. [\#958](https://github.com/sbt/sbt-native-packager/pull/958) ([eed3si9n](https://github.com/eed3si9n))
- Add new system loader setting for file descriptor limit [\#954](https://github.com/sbt/sbt-native-packager/pull/954) ([levinson](https://github.com/levinson))
- Some trivial formatting fix \(tabs -\> spaces\) [\#951](https://github.com/sbt/sbt-native-packager/pull/951) ([GreyCat](https://github.com/GreyCat))
- Fix \#947 Correct scope for packageName in DockerPlugin [\#948](https://github.com/sbt/sbt-native-packager/pull/948) ([muuki88](https://github.com/muuki88))
- Define addJava in ash-template [\#944](https://github.com/sbt/sbt-native-packager/pull/944) ([muuki88](https://github.com/muuki88))
- Drop "in Docker" in some docker keys [\#939](https://github.com/sbt/sbt-native-packager/pull/939) ([dwijnand](https://github.com/dwijnand))
- Allow custom templates to be provided separately for each template [\#938](https://github.com/sbt/sbt-native-packager/pull/938) ([ANorwell](https://github.com/ANorwell))

## [v1.2.0-M8](https://github.com/sbt/sbt-native-packager/tree/v1.2.0-M8) (2017-01-25)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.2.0-M7...v1.2.0-M8)

**Fixed bugs:**

- bash-forwarder-template should define the same header as bash-template [\#921](https://github.com/sbt/sbt-native-packager/issues/921)
- universal:packageBin not working with spaces in path \(windows / play application\) [\#913](https://github.com/sbt/sbt-native-packager/issues/913)
- brp-java-repack-jars still running despite setting rpmBrpJavaRepackJars to false [\#789](https://github.com/sbt/sbt-native-packager/issues/789)

**Closed issues:**

- Docker base image is deprecated [\#928](https://github.com/sbt/sbt-native-packager/issues/928)
- dockerEntrypoint has no effect in 1.1.4 and 1.1.5 [\#927](https://github.com/sbt/sbt-native-packager/issues/927)
- Native package heavily uses and recommends `\<+=`, which is deprecated [\#919](https://github.com/sbt/sbt-native-packager/issues/919)
- s [\#915](https://github.com/sbt/sbt-native-packager/issues/915)
- Multi-project build not working [\#914](https://github.com/sbt/sbt-native-packager/issues/914)
- rpm help show summary of linux default config [\#912](https://github.com/sbt/sbt-native-packager/issues/912)
- please publish to sonatype or somehow sync with maven central [\#906](https://github.com/sbt/sbt-native-packager/issues/906)

**Merged pull requests:**

- Fixes issue \#789 where brpJavaRepack was negated [\#932](https://github.com/sbt/sbt-native-packager/pull/932) ([YuvalItzchakov](https://github.com/YuvalItzchakov))
- Wip/remove symbolic operators [\#931](https://github.com/sbt/sbt-native-packager/pull/931) ([muuki88](https://github.com/muuki88))
- Fix appveyor build [\#930](https://github.com/sbt/sbt-native-packager/pull/930) ([muuki88](https://github.com/muuki88))
- Use stage  task instead of debianExplodedPackage [\#925](https://github.com/sbt/sbt-native-packager/pull/925) ([muuki88](https://github.com/muuki88))
- Change shebang in bash-forwarder-template to `\#!/usr/bin/env bash`. [\#923](https://github.com/sbt/sbt-native-packager/pull/923) ([jan0sch](https://github.com/jan0sch))
- \[rpm\] Fix configWithNoReplace docs [\#920](https://github.com/sbt/sbt-native-packager/pull/920) ([kodemaniak](https://github.com/kodemaniak))
- If mappings are empty don't add them to the linuxPackageMappings [\#917](https://github.com/sbt/sbt-native-packager/pull/917) ([muuki88](https://github.com/muuki88))
- "docker push" should support dockerExecCommand [\#911](https://github.com/sbt/sbt-native-packager/pull/911) ([rbellamy](https://github.com/rbellamy))
- Add dockerExecCommand setting [\#910](https://github.com/sbt/sbt-native-packager/pull/910) ([rbellamy](https://github.com/rbellamy))

## [v1.2.0-M7](https://github.com/sbt/sbt-native-packager/tree/v1.2.0-M7) (2016-11-12)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.1.5...v1.2.0-M7)

**Merged pull requests:**

- undeprecate generateComponentsAndDirectoryXml in WixHelper [\#909](https://github.com/sbt/sbt-native-packager/pull/909) ([SethTisue](https://github.com/SethTisue))
- DockerAlias should have version scoped to Docker [\#905](https://github.com/sbt/sbt-native-packager/pull/905) ([rbellamy](https://github.com/rbellamy))
- fix a bug in the ash-template [\#897](https://github.com/sbt/sbt-native-packager/pull/897) ([stew](https://github.com/stew))
- RpmNoReplaceplugin and LinuxMappingDSL for "noreplace" configs [\#896](https://github.com/sbt/sbt-native-packager/pull/896) ([kodemaniak](https://github.com/kodemaniak))
- \[rpm\] Fixes \#894. Symlinks only removed during uninstall, not during update of RPM. [\#895](https://github.com/sbt/sbt-native-packager/pull/895) ([kodemaniak](https://github.com/kodemaniak))
- Fixed SystemVPlugin override start script behavior for debian [\#893](https://github.com/sbt/sbt-native-packager/pull/893) ([mitch-seymour](https://github.com/mitch-seymour))
- WIP 633 Add prototype for multiple apps in single project [\#839](https://github.com/sbt/sbt-native-packager/pull/839) ([muuki88](https://github.com/muuki88))

## [v1.1.5](https://github.com/sbt/sbt-native-packager/tree/v1.1.5) (2016-11-12)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.2.0-M6...v1.1.5)

**Closed issues:**

- install rpm get some error [\#907](https://github.com/sbt/sbt-native-packager/issues/907)
- Ability to combine multiple commands in one RUN statement? [\#904](https://github.com/sbt/sbt-native-packager/issues/904)
- Universal App - Can't find resources [\#899](https://github.com/sbt/sbt-native-packager/issues/899)
- During RPM \(yum\) upgrade, config gets removed \(1.2.0-M6\) [\#894](https://github.com/sbt/sbt-native-packager/issues/894)
- dist at windows bat file exec error [\#872](https://github.com/sbt/sbt-native-packager/issues/872)
- Fix deprecation warnings in windows WixHelper [\#726](https://github.com/sbt/sbt-native-packager/issues/726)
- Marking default config files as noreplace in RPMs [\#572](https://github.com/sbt/sbt-native-packager/issues/572)

## [v1.2.0-M6](https://github.com/sbt/sbt-native-packager/tree/v1.2.0-M6) (2016-10-08)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.2.0-M5...v1.2.0-M6)

**Closed issues:**

- UNRESOLVED DEPENDENCIES - org.scala-sbt\#sbt;0.13.12 not found [\#887](https://github.com/sbt/sbt-native-packager/issues/887)
- universalArchiveOptions setting tar CLI command arguments: incorrect order \(maybe\) [\#886](https://github.com/sbt/sbt-native-packager/issues/886)
- Add all jars in lib directory to classpath [\#885](https://github.com/sbt/sbt-native-packager/issues/885)
- Dockerfile/java and Dockerfile/openjdk do not exist [\#879](https://github.com/sbt/sbt-native-packager/issues/879)
- Use a Dockerfile rather than Scala? [\#875](https://github.com/sbt/sbt-native-packager/issues/875)
- Documentation of dockerUpdateLatest needs updated [\#871](https://github.com/sbt/sbt-native-packager/issues/871)
- Is there a way to retrieve the path/filename of the generated rpm? [\#870](https://github.com/sbt/sbt-native-packager/issues/870)
- System Loader scripts are not being generated [\#869](https://github.com/sbt/sbt-native-packager/issues/869)
- Support UDP ports for dockerExposedPorts [\#843](https://github.com/sbt/sbt-native-packager/issues/843)
- JAVA\_OPTS support for ash-template [\#738](https://github.com/sbt/sbt-native-packager/issues/738)

**Merged pull requests:**

- Adding rpm override script tests [\#891](https://github.com/sbt/sbt-native-packager/pull/891) ([mitch-seymour](https://github.com/mitch-seymour))
- Updated cheatsheet with the correct file path for overriding service manager scripts [\#890](https://github.com/sbt/sbt-native-packager/pull/890) ([mitch-seymour](https://github.com/mitch-seymour))
- Update documentation with warning about overriding default tar options. [\#889](https://github.com/sbt/sbt-native-packager/pull/889) ([mackler](https://github.com/mackler))
- Generate EXPOSE when only UDP ports are exposed [\#888](https://github.com/sbt/sbt-native-packager/pull/888) ([lustefaniak](https://github.com/lustefaniak))
- Support for absolute paths in classpath [\#882](https://github.com/sbt/sbt-native-packager/pull/882) ([hayssams](https://github.com/hayssams))
- Add ability to expose UDP ports [\#881](https://github.com/sbt/sbt-native-packager/pull/881) ([NeQuissimus](https://github.com/NeQuissimus))
- Remove dockerfile/ prefix from openjdk images [\#880](https://github.com/sbt/sbt-native-packager/pull/880) ([NeQuissimus](https://github.com/NeQuissimus))
- Replace deprecated java Docker image with openjdk [\#877](https://github.com/sbt/sbt-native-packager/pull/877) ([NeQuissimus](https://github.com/NeQuissimus))
- Fixed wrong description of daemonGroup [\#876](https://github.com/sbt/sbt-native-packager/pull/876) ([PavelPenkov](https://github.com/PavelPenkov))
- Updated dockerUpdateLatest documentation to indicate minimum docker v… [\#874](https://github.com/sbt/sbt-native-packager/pull/874) ([thrykol](https://github.com/thrykol))
- Clarify and fix grammar for Formats docs [\#867](https://github.com/sbt/sbt-native-packager/pull/867) ([weedySeaDragon](https://github.com/weedySeaDragon))
- Updated systemloaders.rst [\#865](https://github.com/sbt/sbt-native-packager/pull/865) ([martinstuder](https://github.com/martinstuder))
- Introduction:  clarify and fix grammar [\#863](https://github.com/sbt/sbt-native-packager/pull/863) ([weedySeaDragon](https://github.com/weedySeaDragon))

## [v1.2.0-M5](https://github.com/sbt/sbt-native-packager/tree/v1.2.0-M5) (2016-08-07)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.2.0-M4...v1.2.0-M5)

**Fixed bugs:**

- Syntax Error on RPM Uninstall [\#855](https://github.com/sbt/sbt-native-packager/issues/855)

**Closed issues:**

- Flag '-f' for docker tag no longer exists in docker version 1.12 [\#859](https://github.com/sbt/sbt-native-packager/issues/859)
- If there are two 'App' objects in project, `docker:publishLocal` task will silently produce invalid image without `bin/app-name` file [\#858](https://github.com/sbt/sbt-native-packager/issues/858)

**Merged pull requests:**

- FIX \#855 Syntax Error on RPM Uninstall [\#860](https://github.com/sbt/sbt-native-packager/pull/860) ([muuki88](https://github.com/muuki88))
- Adding 'dockerBuild{Command, Options}' and renaming 'dockerTag' [\#854](https://github.com/sbt/sbt-native-packager/pull/854) ([makubi](https://github.com/makubi))
- Fix scoping in RPM plugin for \#789 [\#826](https://github.com/sbt/sbt-native-packager/pull/826) ([thetristan](https://github.com/thetristan))

## [v1.2.0-M4](https://github.com/sbt/sbt-native-packager/tree/v1.2.0-M4) (2016-07-26)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.1.4...v1.2.0-M4)

**Closed issues:**

- serverAutostart not available to configure [\#852](https://github.com/sbt/sbt-native-packager/issues/852)
- Publishing with Docker in Windows / 1.0.4 [\#849](https://github.com/sbt/sbt-native-packager/issues/849)

**Merged pull requests:**

- Rename dockerTarget to dockerTag [\#851](https://github.com/sbt/sbt-native-packager/pull/851) ([makubi](https://github.com/makubi))
- Fix: `exec` the java entrypoint in ash-template [\#850](https://github.com/sbt/sbt-native-packager/pull/850) ([yfyf](https://github.com/yfyf))
- Issue 807 - add support to enable/disable service autostart [\#847](https://github.com/sbt/sbt-native-packager/pull/847) ([dpennell](https://github.com/dpennell))
- concners -\> connerns introduction.rst [\#837](https://github.com/sbt/sbt-native-packager/pull/837) ([hedefalk](https://github.com/hedefalk))
- FIX \#770 add ExitStatusSuccess setting for systemd [\#834](https://github.com/sbt/sbt-native-packager/pull/834) ([muuki88](https://github.com/muuki88))
- maintainer file names were missing [\#805](https://github.com/sbt/sbt-native-packager/pull/805) ([zoosky](https://github.com/zoosky))

## [v1.1.4](https://github.com/sbt/sbt-native-packager/tree/v1.1.4) (2016-07-17)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.1.3...v1.1.4)

**Closed issues:**

- docker:publishLocal results in error with Docker 1.12.0-rc3 [\#838](https://github.com/sbt/sbt-native-packager/issues/838)
- Disable service autostart during package install [\#807](https://github.com/sbt/sbt-native-packager/issues/807)

**Merged pull requests:**

- Fix typo in debian systemv script [\#846](https://github.com/sbt/sbt-native-packager/pull/846) ([mattmonkey83](https://github.com/mattmonkey83))

## [v1.1.3](https://github.com/sbt/sbt-native-packager/tree/v1.1.3) (2016-07-12)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.1.2...v1.1.3)

**Closed issues:**

- DockerPlugin overwrites version [\#830](https://github.com/sbt/sbt-native-packager/issues/830)
- Handle SIGTERM in service archetypes [\#770](https://github.com/sbt/sbt-native-packager/issues/770)

**Merged pull requests:**

- docker: tag -f is deprecated \(backport to 1.1.x\) [\#845](https://github.com/sbt/sbt-native-packager/pull/845) ([maciej](https://github.com/maciej))
- Make `sbt docker:stage` work on Windows [\#842](https://github.com/sbt/sbt-native-packager/pull/842) ([oporkka](https://github.com/oporkka))

## [v1.1.2](https://github.com/sbt/sbt-native-packager/tree/v1.1.2) (2016-07-02)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.2.0-M3...v1.1.2)

**Closed issues:**

- How to customize launcher script's `-h` output to include my application's own options? [\#831](https://github.com/sbt/sbt-native-packager/issues/831)
- Parametrized java home [\#816](https://github.com/sbt/sbt-native-packager/issues/816)
- Add docs how to build and test sbt-native-packager [\#810](https://github.com/sbt/sbt-native-packager/issues/810)
- Dead pidfile on wheezy [\#808](https://github.com/sbt/sbt-native-packager/issues/808)

**Merged pull requests:**

- V.1.1 This is a fix for \#812  [\#835](https://github.com/sbt/sbt-native-packager/pull/835) ([zoosky](https://github.com/zoosky))

## [v1.2.0-M3](https://github.com/sbt/sbt-native-packager/tree/v1.2.0-M3) (2016-06-24)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.2.0-M2...v1.2.0-M3)

**Fixed bugs:**

- docker: tag -f is deprecated [\#818](https://github.com/sbt/sbt-native-packager/issues/818)
- RPM scriptles from file produce duplicate entries. [\#812](https://github.com/sbt/sbt-native-packager/issues/812)

**Closed issues:**

- `sbt docker:stage` fails on Windows 10 [\#823](https://github.com/sbt/sbt-native-packager/issues/823)
- Native Packager Logo [\#762](https://github.com/sbt/sbt-native-packager/issues/762)

**Merged pull requests:**

- Fix duplicate RPM scriptlet override [\#825](https://github.com/sbt/sbt-native-packager/pull/825) ([fsat](https://github.com/fsat))
- Make `sbt docker:stage` work on Windows [\#824](https://github.com/sbt/sbt-native-packager/pull/824) ([oporkka](https://github.com/oporkka))
- Expand the java home argument [\#822](https://github.com/sbt/sbt-native-packager/pull/822) ([cquiroz](https://github.com/cquiroz))
- docker: tag -f is deprecated [\#821](https://github.com/sbt/sbt-native-packager/pull/821) ([sgrankin](https://github.com/sgrankin))
- FIX \#762 Add native packager logo to docs and README [\#820](https://github.com/sbt/sbt-native-packager/pull/820) ([muuki88](https://github.com/muuki88))
- Report compression issue with Debian Wheezy's python-apt [\#811](https://github.com/sbt/sbt-native-packager/pull/811) ([jpic](https://github.com/jpic))
- fix typo rpmScriptletsDirectory [\#804](https://github.com/sbt/sbt-native-packager/pull/804) ([zoosky](https://github.com/zoosky))

## [v1.2.0-M2](https://github.com/sbt/sbt-native-packager/tree/v1.2.0-M2) (2016-06-09)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.2.0-M1...v1.2.0-M2)

**Fixed bugs:**

- \[bug\] wrong systemd script file path in debian [\#679](https://github.com/sbt/sbt-native-packager/issues/679)

**Closed issues:**

- Rpm and Deb packaging is missing 'provides' and 'conflicts' tags/sections [\#801](https://github.com/sbt/sbt-native-packager/issues/801)
- Multimodule: `docker:publish` error in virtual root [\#800](https://github.com/sbt/sbt-native-packager/issues/800)
- Scoping for docker-related keys in DockerPlugin [\#796](https://github.com/sbt/sbt-native-packager/issues/796)
- Update scalariform [\#790](https://github.com/sbt/sbt-native-packager/issues/790)
- Upstart script does not use killTimeout [\#754](https://github.com/sbt/sbt-native-packager/issues/754)

**Merged pull requests:**

- Support for 'Provides' and 'Conflicts' sections for debian packaging [\#803](https://github.com/sbt/sbt-native-packager/pull/803) ([412b](https://github.com/412b))
- Introduce new namespace and solve duplicate key issue [\#802](https://github.com/sbt/sbt-native-packager/pull/802) ([smoes](https://github.com/smoes))
- Fix \#754 Add kill timeout to upstart. Add retryTimeout and retries keys [\#799](https://github.com/sbt/sbt-native-packager/pull/799) ([muuki88](https://github.com/muuki88))
- Fix typo in debian systemv script. [\#798](https://github.com/sbt/sbt-native-packager/pull/798) ([jan0sch](https://github.com/jan0sch))
- Use recommended path for systemd scripts [\#795](https://github.com/sbt/sbt-native-packager/pull/795) ([muuki88](https://github.com/muuki88))
- Wip/upgrade build [\#794](https://github.com/sbt/sbt-native-packager/pull/794) ([muuki88](https://github.com/muuki88))

## [v1.2.0-M1](https://github.com/sbt/sbt-native-packager/tree/v1.2.0-M1) (2016-05-22)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.1.1...v1.2.0-M1)

**Merged pull requests:**

- Extract Systemloaders into AutoPlugins [\#785](https://github.com/sbt/sbt-native-packager/pull/785) ([muuki88](https://github.com/muuki88))

## [v1.1.1](https://github.com/sbt/sbt-native-packager/tree/v1.1.1) (2016-05-19)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.1.0...v1.1.1)

**Closed issues:**

- Usage of JavaAppPackaging scriptClasspath [\#793](https://github.com/sbt/sbt-native-packager/issues/793)
- Docker commands are not in order [\#791](https://github.com/sbt/sbt-native-packager/issues/791)
- add support for building 32-bit RPMs on 64-bit Linux [\#784](https://github.com/sbt/sbt-native-packager/issues/784)
- Add a way to specify a docker command / docker binary [\#783](https://github.com/sbt/sbt-native-packager/issues/783)
- sbt docker:publishLocal fails on Windows  [\#760](https://github.com/sbt/sbt-native-packager/issues/760)

**Merged pull requests:**

- fix \#766 [\#792](https://github.com/sbt/sbt-native-packager/pull/792) ([giabao](https://github.com/giabao))
- Don't compress debian packages [\#787](https://github.com/sbt/sbt-native-packager/pull/787) ([pauldraper](https://github.com/pauldraper))
- Add rpmSetarch support [\#786](https://github.com/sbt/sbt-native-packager/pull/786) ([dpennell](https://github.com/dpennell))
- daemonStdoutLogFile setting implemented [\#772](https://github.com/sbt/sbt-native-packager/pull/772) ([kardapoltsev](https://github.com/kardapoltsev))
- Make Docker plugin portable so that Windows is supported [\#765](https://github.com/sbt/sbt-native-packager/pull/765) ([mkotsbak](https://github.com/mkotsbak))

## [v1.1.0](https://github.com/sbt/sbt-native-packager/tree/v1.1.0) (2016-04-24)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.1.0-RC3...v1.1.0)

**Fixed bugs:**

- chown: invalid group `user:user` [\#776](https://github.com/sbt/sbt-native-packager/issues/776)

**Closed issues:**

- Add documentation for multiple package outputs with different config [\#777](https://github.com/sbt/sbt-native-packager/issues/777)
- docker - var in from instruction [\#775](https://github.com/sbt/sbt-native-packager/issues/775)
- Support for Alpine Linux with only sh [\#773](https://github.com/sbt/sbt-native-packager/issues/773)
- Bat-Template expects @APP\_ENV\_NAME@@\_config.txt rather than application.ini [\#768](https://github.com/sbt/sbt-native-packager/issues/768)

**Merged pull requests:**

- task name is rpm-lint [\#779](https://github.com/sbt/sbt-native-packager/pull/779) ([zoosky](https://github.com/zoosky))
- Fix \#776 using $DAEMON\_GROUP to indicate group in Redhat startup temp… [\#778](https://github.com/sbt/sbt-native-packager/pull/778) ([owenfeehan](https://github.com/owenfeehan))
- Add hint for windows-users \(configuration\) [\#774](https://github.com/sbt/sbt-native-packager/pull/774) ([wofr](https://github.com/wofr))

## [v1.1.0-RC3](https://github.com/sbt/sbt-native-packager/tree/v1.1.0-RC3) (2016-04-09)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.1.0-RC2...v1.1.0-RC3)

**Fixed bugs:**

- Link to non-existing page `np-plugin.html` [\#740](https://github.com/sbt/sbt-native-packager/issues/740)
- javaOptions should not come after mainclass  [\#598](https://github.com/sbt/sbt-native-packager/issues/598)
- Wrong debian systemv init script exit codes [\#552](https://github.com/sbt/sbt-native-packager/issues/552)
- Execute flag missing from bin/prog with sbt task universal:packageBin when zipped and unzipped in OS X [\#334](https://github.com/sbt/sbt-native-packager/issues/334)
- Packager fails when using a changed target directory [\#289](https://github.com/sbt/sbt-native-packager/issues/289)
- Cannot create rpms with no files in them [\#16](https://github.com/sbt/sbt-native-packager/issues/16)

**Closed issues:**

- How to specify owner of created /usr/share/\<project\> in systemd unit [\#769](https://github.com/sbt/sbt-native-packager/issues/769)
- rpmVendor not honored [\#764](https://github.com/sbt/sbt-native-packager/issues/764)
- “module not found: org.scala-sbt\#sbt;0.13.9” after installing from Debian repo [\#763](https://github.com/sbt/sbt-native-packager/issues/763)
- Duplication of jars in packaged zip artifact \(unpredictable\) [\#759](https://github.com/sbt/sbt-native-packager/issues/759)
- JDebPackaging does not apply name, maintainer, ... [\#758](https://github.com/sbt/sbt-native-packager/issues/758)
- Customize for configuration [\#752](https://github.com/sbt/sbt-native-packager/issues/752)
- v1.0.4 documentation for customized boot scripts [\#750](https://github.com/sbt/sbt-native-packager/issues/750)
- Should employ mappings in ThisScope instead of mappings in Universal [\#746](https://github.com/sbt/sbt-native-packager/issues/746)
- Stdout in SystemV init script [\#722](https://github.com/sbt/sbt-native-packager/issues/722)
- Create symbolic links before starting the service. [\#718](https://github.com/sbt/sbt-native-packager/issues/718)
- Unable to publish RPM for a Play 2.4 app [\#706](https://github.com/sbt/sbt-native-packager/issues/706)
- sbt packager \( esp. Debian plugin\) does not package scalajs-files and webjars [\#699](https://github.com/sbt/sbt-native-packager/issues/699)
- The primary key 'shortcut\_745d79b4\_272c\_40f1\_866b\_a333e9a1b425710920299\_SC' is duplicated in table 'Shortcut'. [\#692](https://github.com/sbt/sbt-native-packager/issues/692)
- PID must be play.pid [\#683](https://github.com/sbt/sbt-native-packager/issues/683)
- Include examples [\#678](https://github.com/sbt/sbt-native-packager/issues/678)
- Auto plugins are required to declare their project configurations [\#676](https://github.com/sbt/sbt-native-packager/issues/676)
- Fix links in java\_app [\#671](https://github.com/sbt/sbt-native-packager/issues/671)
- stage unnecessarily builds javadoc jar and is slow [\#651](https://github.com/sbt/sbt-native-packager/issues/651)
- Presense of several main classes breaks docker:publishLocal [\#636](https://github.com/sbt/sbt-native-packager/issues/636)
- Extend docs for sbt-aether and sbt-native-packager [\#617](https://github.com/sbt/sbt-native-packager/issues/617)
- Using custom postinst script with JavaServerAppPackaging overrides maintainer one [\#576](https://github.com/sbt/sbt-native-packager/issues/576)
- scriptlets in rpm .spec file are duplicated [\#575](https://github.com/sbt/sbt-native-packager/issues/575)
- sbt-native-packager docs [\#574](https://github.com/sbt/sbt-native-packager/issues/574)
- Evaluate Spotify DockerClient [\#558](https://github.com/sbt/sbt-native-packager/issues/558)
- Imports are wrong in Getting started doc [\#511](https://github.com/sbt/sbt-native-packager/issues/511)
- Installing generated rpm on a clean centos \(6.6 and 7.0\) [\#484](https://github.com/sbt/sbt-native-packager/issues/484)
- copy .war file from ivy cache to lib folder [\#414](https://github.com/sbt/sbt-native-packager/issues/414)
- description in upstart config should be taken from packageDescription not from packageSummary [\#384](https://github.com/sbt/sbt-native-packager/issues/384)
- setting for scriplets in rpm files should be Seq\[String\] instead of Option\[String\] [\#269](https://github.com/sbt/sbt-native-packager/issues/269)

**Merged pull requests:**

- Add a few docker tests for travis [\#771](https://github.com/sbt/sbt-native-packager/pull/771) ([muuki88](https://github.com/muuki88))
- Fix an obvious typo [\#767](https://github.com/sbt/sbt-native-packager/pull/767) ([GreyCat](https://github.com/GreyCat))
- AshScriptPlugin - Restore argument behavior as it was before a037519 [\#766](https://github.com/sbt/sbt-native-packager/pull/766) ([dhoepelman](https://github.com/dhoepelman))
- FIX \#676 adding configuration scopes to project configurations [\#757](https://github.com/sbt/sbt-native-packager/pull/757) ([muuki88](https://github.com/muuki88))
- Major documentation clean up and preps for readthedocs [\#755](https://github.com/sbt/sbt-native-packager/pull/755) ([muuki88](https://github.com/muuki88))
- FIX \#750 remove outdated documentation [\#751](https://github.com/sbt/sbt-native-packager/pull/751) ([muuki88](https://github.com/muuki88))
- Updates version of DockerSpotifyClient to 3.5.13 and fixes NullPointerException [\#749](https://github.com/sbt/sbt-native-packager/pull/749) ([vsuharnikov](https://github.com/vsuharnikov))
- Fix errors when .bat file path contains paranthesis. [\#748](https://github.com/sbt/sbt-native-packager/pull/748) ([szdmr](https://github.com/szdmr))

## [v1.1.0-RC2](https://github.com/sbt/sbt-native-packager/tree/v1.1.0-RC2) (2016-02-23)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.1.0-RC1...v1.1.0-RC2)

**Closed issues:**

- unidoc compile issue [\#742](https://github.com/sbt/sbt-native-packager/issues/742)
- systemd scripts completely ignoring `/etc/default/{{package-name}}` [\#737](https://github.com/sbt/sbt-native-packager/issues/737)
- Add option to totally remove RUNNING\_PID management [\#736](https://github.com/sbt/sbt-native-packager/issues/736)
- r [\#735](https://github.com/sbt/sbt-native-packager/issues/735)
- docker,package name should not support upper case? [\#725](https://github.com/sbt/sbt-native-packager/issues/725)

**Merged pull requests:**

- SystemD services now source /etc/default/{{app\_name}} \(resolves \#737\) [\#745](https://github.com/sbt/sbt-native-packager/pull/745) ([timcharper](https://github.com/timcharper))
- Fix typo in description of maintainerScripts [\#743](https://github.com/sbt/sbt-native-packager/pull/743) ([fthomas](https://github.com/fthomas))
- Readme cleanup [\#741](https://github.com/sbt/sbt-native-packager/pull/741) ([muuki88](https://github.com/muuki88))
- Docker Plugin qualification [\#733](https://github.com/sbt/sbt-native-packager/pull/733) ([matthughes](https://github.com/matthughes))

## [v1.1.0-RC1](https://github.com/sbt/sbt-native-packager/tree/v1.1.0-RC1) (2016-01-23)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.1.0-M3...v1.1.0-RC1)

**Fixed bugs:**

- 1.0.5 regression: tarball tasks run `tar` with incorrect options \(namely, none\) [\#731](https://github.com/sbt/sbt-native-packager/issues/731)

**Closed issues:**

- exec bin/\*\*\* no such file [\#730](https://github.com/sbt/sbt-native-packager/issues/730)

**Merged pull requests:**

- FIX \#731 add options for zip and tarballs [\#732](https://github.com/sbt/sbt-native-packager/pull/732) ([muuki88](https://github.com/muuki88))

## [v1.1.0-M3](https://github.com/sbt/sbt-native-packager/tree/v1.1.0-M3) (2016-01-09)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.1.0-M2...v1.1.0-M3)

## [v1.1.0-M2](https://github.com/sbt/sbt-native-packager/tree/v1.1.0-M2) (2016-01-09)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.1.0-M1...v1.1.0-M2)

**Fixed bugs:**

- Adding ant dependency for jdkpackager builds. Follow up to \#719 [\#720](https://github.com/sbt/sbt-native-packager/pull/720) ([muuki88](https://github.com/muuki88))

**Merged pull requests:**

- Add Proguard example to Custom Package Format documentation [\#721](https://github.com/sbt/sbt-native-packager/pull/721) ([mikebridge](https://github.com/mikebridge))
- Mark docker and jdeb as provided dependencies [\#719](https://github.com/sbt/sbt-native-packager/pull/719) ([muuki88](https://github.com/muuki88))

## [v1.1.0-M1](https://github.com/sbt/sbt-native-packager/tree/v1.1.0-M1) (2015-12-13)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.0.6...v1.1.0-M1)

**Fixed bugs:**

- application.ini incorrect line ending =\> application can't start when build from Windows & run in Linux [\#637](https://github.com/sbt/sbt-native-packager/issues/637)

**Closed issues:**

- docker plugin should require JavaAppPackaging [\#712](https://github.com/sbt/sbt-native-packager/issues/712)

**Merged pull requests:**

- Travis: build on OS X \(take 2\) [\#715](https://github.com/sbt/sbt-native-packager/pull/715) ([larsrh](https://github.com/larsrh))
- Bumped version number in Installation section. [\#713](https://github.com/sbt/sbt-native-packager/pull/713) ([metasim](https://github.com/metasim))
- Unify maintainerScripts [\#625](https://github.com/sbt/sbt-native-packager/pull/625) ([muuki88](https://github.com/muuki88))

## [v1.0.6](https://github.com/sbt/sbt-native-packager/tree/v1.0.6) (2015-12-06)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.0.5...v1.0.6)

**Closed issues:**

- Boot scripts are not generated when using mainClass in \(Compile, run\) instead of only Compile [\#710](https://github.com/sbt/sbt-native-packager/issues/710)
- Unable to set correct ownership on an RPM installation [\#709](https://github.com/sbt/sbt-native-packager/issues/709)
- javaOptions in Universal ignored by AshScriptPlugin [\#702](https://github.com/sbt/sbt-native-packager/issues/702)

**Merged pull requests:**

- Ensure systemd works with RPM relocatable packages [\#711](https://github.com/sbt/sbt-native-packager/pull/711) ([fsat](https://github.com/fsat))
- Improve docs on deploying different pkg formats [\#708](https://github.com/sbt/sbt-native-packager/pull/708) ([umatrangolo](https://github.com/umatrangolo))
- Update README URLs based on HTTP redirects [\#705](https://github.com/sbt/sbt-native-packager/pull/705) ([ReadmeCritic](https://github.com/ReadmeCritic))
- AshScriptPlugin - pass arguments loaded from script conf file [\#704](https://github.com/sbt/sbt-native-packager/pull/704) ([pawelkaczor](https://github.com/pawelkaczor))
- Different start script replacements for different system loaders [\#701](https://github.com/sbt/sbt-native-packager/pull/701) ([kardapoltsev](https://github.com/kardapoltsev))
- Removed default-jre | java6-runtime dependency in jdeb packaging [\#700](https://github.com/sbt/sbt-native-packager/pull/700) ([kardapoltsev](https://github.com/kardapoltsev))
- fix SystemV init script template for debian packaging [\#697](https://github.com/sbt/sbt-native-packager/pull/697) ([yanns](https://github.com/yanns))

## [v1.0.5](https://github.com/sbt/sbt-native-packager/tree/v1.0.5) (2015-11-11)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.0.5-RC1...v1.0.5)

**Merged pull requests:**

- Fix systemv restartService function [\#694](https://github.com/sbt/sbt-native-packager/pull/694) ([finlob](https://github.com/finlob))
- Upgrade spotify docker-client to 3.2.1 [\#693](https://github.com/sbt/sbt-native-packager/pull/693) ([gbougeard](https://github.com/gbougeard))

## [v1.0.5-RC1](https://github.com/sbt/sbt-native-packager/tree/v1.0.5-RC1) (2015-11-07)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.0.5-M3...v1.0.5-RC1)

**Fixed bugs:**

- RPM relocation - requires local ownership of sbt-native-packager source templates [\#659](https://github.com/sbt/sbt-native-packager/issues/659)

**Closed issues:**

- unusable command `rpm:rpmlint`, wrong document [\#690](https://github.com/sbt/sbt-native-packager/issues/690)
- RPM\_BUILD\_ROOT path not correct when use `rpmbuild -bb` [\#686](https://github.com/sbt/sbt-native-packager/issues/686)
- Relative path in DEBIAN/conffiles [\#684](https://github.com/sbt/sbt-native-packager/issues/684)
- can't start with root  [\#682](https://github.com/sbt/sbt-native-packager/issues/682)
- Trying to Deploy the debian package on Ubuntu AMI [\#673](https://github.com/sbt/sbt-native-packager/issues/673)
- JavaAppPackaging can provide pid and stop script [\#670](https://github.com/sbt/sbt-native-packager/issues/670)
- hardcoded RPM log directory [\#619](https://github.com/sbt/sbt-native-packager/issues/619)

**Merged pull requests:**

- Allow to override systemd start script name [\#691](https://github.com/sbt/sbt-native-packager/pull/691) ([rozky](https://github.com/rozky))
- Relocatable rpm symlink fix [\#685](https://github.com/sbt/sbt-native-packager/pull/685) ([fsat](https://github.com/fsat))
- Ensure pidfile dir on systemv rpm template exists [\#680](https://github.com/sbt/sbt-native-packager/pull/680) ([mcarolan](https://github.com/mcarolan))
- Added rpm-build as a Requirement for \*.rpm [\#677](https://github.com/sbt/sbt-native-packager/pull/677) ([schmitch](https://github.com/schmitch))
- defaultLinuxLogsLocation not applied to start-rpm-template [\#675](https://github.com/sbt/sbt-native-packager/pull/675) ([louisq](https://github.com/louisq))
- fix typo on document [\#674](https://github.com/sbt/sbt-native-packager/pull/674) ([schon](https://github.com/schon))
- fix issue \#637 [\#669](https://github.com/sbt/sbt-native-packager/pull/669) ([giabao](https://github.com/giabao))

## [v1.0.5-M3](https://github.com/sbt/sbt-native-packager/tree/v1.0.5-M3) (2015-09-11)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.0.5-M2...v1.0.5-M3)

**Merged pull requests:**

- Fixes for RPM related functionality in 1.0.5-M2 [\#668](https://github.com/sbt/sbt-native-packager/pull/668) ([fsat](https://github.com/fsat))

## [v1.0.5-M2](https://github.com/sbt/sbt-native-packager/tree/v1.0.5-M2) (2015-09-10)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.0.5-M1...v1.0.5-M2)

**Merged pull requests:**

- Reinstate and deprecate older methods to preserve binary backward compatibility with 1.0.4 [\#667](https://github.com/sbt/sbt-native-packager/pull/667) ([fsat](https://github.com/fsat))

## [v1.0.5-M1](https://github.com/sbt/sbt-native-packager/tree/v1.0.5-M1) (2015-09-09)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.0.4...v1.0.5-M1)

**Closed issues:**

- tar is expected to have --force-local option [\#662](https://github.com/sbt/sbt-native-packager/issues/662)
- rpm scriptlets are not read from default rpm script directory [\#660](https://github.com/sbt/sbt-native-packager/issues/660)

**Merged pull requests:**

- FIX \#662 and make universal archive options customizable [\#666](https://github.com/sbt/sbt-native-packager/pull/666) ([muuki88](https://github.com/muuki88))
- Add test for relocatable rpm behaviour [\#664](https://github.com/sbt/sbt-native-packager/pull/664) ([fsat](https://github.com/fsat))
- Support for relocatable RPMs [\#661](https://github.com/sbt/sbt-native-packager/pull/661) ([fsat](https://github.com/fsat))
- Use spotify docker-client for docker:publishLocal goal [\#658](https://github.com/sbt/sbt-native-packager/pull/658) ([gbougeard](https://github.com/gbougeard))
- Make the log file generated by application daemon configurable in RPM based SystemV [\#656](https://github.com/sbt/sbt-native-packager/pull/656) ([fsat](https://github.com/fsat))

## [v1.0.4](https://github.com/sbt/sbt-native-packager/tree/v1.0.4) (2015-08-23)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.0.4-RC1...v1.0.4)

**Fixed bugs:**

- With JDKNativePackager, src/deploy is not on the classpath in the build.xml [\#644](https://github.com/sbt/sbt-native-packager/issues/644)
- Issue with file associations in JDKNativePackager [\#643](https://github.com/sbt/sbt-native-packager/issues/643)
- RPM services fail after reboot on any system where /run is based on tmpfs [\#609](https://github.com/sbt/sbt-native-packager/issues/609)

**Closed issues:**

- Unable to correctly name binary [\#649](https://github.com/sbt/sbt-native-packager/issues/649)
- Ability to specify additional files to JDKNativePackager [\#642](https://github.com/sbt/sbt-native-packager/issues/642)
- How to "run" Java Application Archetype? [\#640](https://github.com/sbt/sbt-native-packager/issues/640)
- Overridable bash template [\#635](https://github.com/sbt/sbt-native-packager/issues/635)
- Docker fails to build container [\#634](https://github.com/sbt/sbt-native-packager/issues/634)
- No conf folder on the classpath when using ClasspathJarPlugin or LauncherJarPlugin [\#624](https://github.com/sbt/sbt-native-packager/issues/624)
- RPM install fails on RHEL 5 [\#621](https://github.com/sbt/sbt-native-packager/issues/621)
- Feedback on using JDKPackagerPlugin for some days \(creating MSI\) [\#594](https://github.com/sbt/sbt-native-packager/issues/594)

**Merged pull requests:**

- Fixed start script directory in start-rpm-template [\#653](https://github.com/sbt/sbt-native-packager/pull/653) ([kardapoltsev](https://github.com/kardapoltsev))
- ash support documentation [\#652](https://github.com/sbt/sbt-native-packager/pull/652) ([gzoller](https://github.com/gzoller))
- Improved rpm scripts for systemd [\#650](https://github.com/sbt/sbt-native-packager/pull/650) ([Zarratustra](https://github.com/Zarratustra))
- Fixed variable names and capitalization [\#648](https://github.com/sbt/sbt-native-packager/pull/648) ([giampaolotrapasso](https://github.com/giampaolotrapasso))
- wip/ash [\#647](https://github.com/sbt/sbt-native-packager/pull/647) ([gzoller](https://github.com/gzoller))
- overridable bash and bat templates [\#646](https://github.com/sbt/sbt-native-packager/pull/646) ([dvic](https://github.com/dvic))
- Fix for \#644 \(`src/deploy` in Ant classpath\). [\#645](https://github.com/sbt/sbt-native-packager/pull/645) ([metasim](https://github.com/metasim))
- FIX \#621 --system option for user/groupadd replaced with -r. [\#631](https://github.com/sbt/sbt-native-packager/pull/631) ([kardapoltsev](https://github.com/kardapoltsev))
- Update start-template for systemd [\#629](https://github.com/sbt/sbt-native-packager/pull/629) ([kononencheg](https://github.com/kononencheg))

## [v1.0.4-RC1](https://github.com/sbt/sbt-native-packager/tree/v1.0.4-RC1) (2015-07-24)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.0.3...v1.0.4-RC1)

**Fixed bugs:**

- Play docs are wrong [\#607](https://github.com/sbt/sbt-native-packager/issues/607)
- daemon user removed while perform rpm upgrading [\#529](https://github.com/sbt/sbt-native-packager/issues/529)

**Closed issues:**

- relocatable RPMs? [\#618](https://github.com/sbt/sbt-native-packager/issues/618)
- How to force RPM v3 signature? [\#616](https://github.com/sbt/sbt-native-packager/issues/616)
- Systemd deb package does not restart on install / redeploy / host restart [\#610](https://github.com/sbt/sbt-native-packager/issues/610)
- Issue in template [\#606](https://github.com/sbt/sbt-native-packager/issues/606)
- docker packager ignores settings [\#605](https://github.com/sbt/sbt-native-packager/issues/605)
- Windows doc page has many RPM / Linux references [\#590](https://github.com/sbt/sbt-native-packager/issues/590)

**Merged pull requests:**

- Added CombinedCmd and removed EnvCmd [\#628](https://github.com/sbt/sbt-native-packager/pull/628) ([ipsq](https://github.com/ipsq))
- Added EnvCmd [\#627](https://github.com/sbt/sbt-native-packager/pull/627) ([ipsq](https://github.com/ipsq))
- Better formatting for message [\#626](https://github.com/sbt/sbt-native-packager/pull/626) ([ipsq](https://github.com/ipsq))
- use autopluging system in deployments docs [\#615](https://github.com/sbt/sbt-native-packager/pull/615) ([szimano](https://github.com/szimano))
- Deployment section, documentation fix [\#614](https://github.com/sbt/sbt-native-packager/pull/614) ([szimano](https://github.com/szimano))
- Correct the way to give additional informations for Play apps [\#613](https://github.com/sbt/sbt-native-packager/pull/613) ([nremond](https://github.com/nremond))
- Indicate the 1.0.3 release as the last one [\#612](https://github.com/sbt/sbt-native-packager/pull/612) ([nremond](https://github.com/nremond))
- If the /var/run \(aka /run\) folder doesn't exist for the PID file, create it [\#611](https://github.com/sbt/sbt-native-packager/pull/611) ([ekuns](https://github.com/ekuns))
- Add note about JDeb adding dependencies [\#608](https://github.com/sbt/sbt-native-packager/pull/608) ([philwills](https://github.com/philwills))
- tar --force-local on windows [\#604](https://github.com/sbt/sbt-native-packager/pull/604) ([sumkincpp](https://github.com/sumkincpp))
- FIX \#590 fixed irritating windows documentation [\#592](https://github.com/sbt/sbt-native-packager/pull/592) ([muuki88](https://github.com/muuki88))

## [v1.0.3](https://github.com/sbt/sbt-native-packager/tree/v1.0.3) (2015-06-16)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.0.2...v1.0.3)

**Fixed bugs:**

- application.ini doesn't get replacements such as ${{app\_name}} [\#599](https://github.com/sbt/sbt-native-packager/issues/599)
- show dockerCommands fails on windows OS [\#573](https://github.com/sbt/sbt-native-packager/issues/573)
- Upstart Defaults Not Working for Play 2.3.8 Under SBT Native Packager 1.0.0 [\#554](https://github.com/sbt/sbt-native-packager/issues/554)
- re \#577 fix for running the bash file in a path with spaces [\#581](https://github.com/sbt/sbt-native-packager/pull/581) ([francisdb](https://github.com/francisdb))

**Closed issues:**

- Service loops itself after installation, PlayFramework 2.4, debian package, ubuntu 14.04 [\#596](https://github.com/sbt/sbt-native-packager/issues/596)
- documentation for Build.scala files [\#593](https://github.com/sbt/sbt-native-packager/issues/593)
- Binary broken when run in path with spaces [\#577](https://github.com/sbt/sbt-native-packager/issues/577)
- Improving docs about application.ini v etc-default [\#559](https://github.com/sbt/sbt-native-packager/issues/559)
- proguard support [\#518](https://github.com/sbt/sbt-native-packager/issues/518)
- Jar with no classes is not included with staged files? [\#347](https://github.com/sbt/sbt-native-packager/issues/347)
- More keys to enhance customization of log and app directories [\#296](https://github.com/sbt/sbt-native-packager/issues/296)
- Support Mac OS X app bundle [\#253](https://github.com/sbt/sbt-native-packager/issues/253)

**Merged pull requests:**

- FIX \#347 add ability to add arbitrary stuff from the classpath. Only [\#603](https://github.com/sbt/sbt-native-packager/pull/603) ([muuki88](https://github.com/muuki88))
- FIX \#599 \#598 Extended docs for application customization [\#601](https://github.com/sbt/sbt-native-packager/pull/601) ([muuki88](https://github.com/muuki88))
- grammar fix for website [\#595](https://github.com/sbt/sbt-native-packager/pull/595) ([SethTisue](https://github.com/SethTisue))
- Documented packageTemplateMappings [\#591](https://github.com/sbt/sbt-native-packager/pull/591) ([kodemaniak](https://github.com/kodemaniak))
- Updated sbt plugin fragment version. [\#589](https://github.com/sbt/sbt-native-packager/pull/589) ([metasim](https://github.com/metasim))
- Use docker on windows \#573 [\#588](https://github.com/sbt/sbt-native-packager/pull/588) ([sjeandeaux](https://github.com/sjeandeaux))

## [v1.0.2](https://github.com/sbt/sbt-native-packager/tree/v1.0.2) (2015-05-20)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.0.1...v1.0.2)

**Fixed bugs:**

- default mainClass not picked up [\#568](https://github.com/sbt/sbt-native-packager/issues/568)

**Closed issues:**

- Create a single page for overriding templates and directory conventions [\#438](https://github.com/sbt/sbt-native-packager/issues/438)
- Primary key duplicated in table 'Shortcut' for Windows builds [\#21](https://github.com/sbt/sbt-native-packager/issues/21)

**Merged pull requests:**

- Conversion of JDKPackager to use JDK-provided Ant tasks. [\#583](https://github.com/sbt/sbt-native-packager/pull/583) ([metasim](https://github.com/metasim))
- Bumped git.baseVersion to next development version. [\#582](https://github.com/sbt/sbt-native-packager/pull/582) ([metasim](https://github.com/metasim))
- Remove `in Docker` from dockerExposedPorts in docs [\#579](https://github.com/sbt/sbt-native-packager/pull/579) ([GitsMcGee](https://github.com/GitsMcGee))
- Compilation warning cleanup, including removal of deprecated octal literals. [\#578](https://github.com/sbt/sbt-native-packager/pull/578) ([metasim](https://github.com/metasim))
- small typo [\#571](https://github.com/sbt/sbt-native-packager/pull/571) ([francisdb](https://github.com/francisdb))
- updated docs related to \#568 on multiple main classes [\#570](https://github.com/sbt/sbt-native-packager/pull/570) ([francisdb](https://github.com/francisdb))
- Wip/issue 21 multiple config files shortcuts [\#569](https://github.com/sbt/sbt-native-packager/pull/569) ([ezzarghili](https://github.com/ezzarghili))

## [v1.0.1](https://github.com/sbt/sbt-native-packager/tree/v1.0.1) (2015-04-29)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.0.0...v1.0.1)

**Fixed bugs:**

- JDKPackagerPlugin fails to build package if maintainer is not set [\#563](https://github.com/sbt/sbt-native-packager/issues/563)
- How to force tag in docker sbt native packager? [\#556](https://github.com/sbt/sbt-native-packager/issues/556)
- Daemon user with /bin/false as shell does not work on CentOS 6.5 [\#515](https://github.com/sbt/sbt-native-packager/issues/515)

**Closed issues:**

- Akka application packaging with logger [\#565](https://github.com/sbt/sbt-native-packager/issues/565)
- \[sbt-native-packager 1.0.0\] \[play 2.3.8\] Debian package installation uses wrong user to start [\#560](https://github.com/sbt/sbt-native-packager/issues/560)
- Error in Docker Plugin Force Latest Parameter Sequence? [\#555](https://github.com/sbt/sbt-native-packager/issues/555)
- Akka archetype bat script needs major rework [\#521](https://github.com/sbt/sbt-native-packager/issues/521)
- None of the plugins are available in build.sbt, so I can't use enablePlugins\(\) on them there [\#516](https://github.com/sbt/sbt-native-packager/issues/516)
- defaultLinuxInstallationLocation should read defaultLinuxInstallLocation [\#504](https://github.com/sbt/sbt-native-packager/issues/504)
- javaagent is not recognized in jvmopts [\#481](https://github.com/sbt/sbt-native-packager/issues/481)
- 0.8.0: JDebPackaging tries to create symlink using 'ln' on windows [\#467](https://github.com/sbt/sbt-native-packager/issues/467)
- Refactor linuxScriptReplacement creation [\#437](https://github.com/sbt/sbt-native-packager/issues/437)
- Documentation is inconsistent with the version in the top bar [\#395](https://github.com/sbt/sbt-native-packager/issues/395)

**Merged pull requests:**

- Additional tests and fix for \#563. [\#566](https://github.com/sbt/sbt-native-packager/pull/566) ([metasim](https://github.com/metasim))
- Use Scala 2.10.5 so that we don't hit SI-7439 as reported in \#557 by @justinsb [\#564](https://github.com/sbt/sbt-native-packager/pull/564) ([benmccann](https://github.com/benmccann))
- Fix debian systemV init script status command \#552 [\#562](https://github.com/sbt/sbt-native-packager/pull/562) ([kardapoltsev](https://github.com/kardapoltsev))

## [v1.0.0](https://github.com/sbt/sbt-native-packager/tree/v1.0.0) (2015-04-11)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.0.0-RC2...v1.0.0)

**Closed issues:**

- dockerEntrypoint not being set in 1.0.0-M1 and 1.0.0-M2 [\#536](https://github.com/sbt/sbt-native-packager/issues/536)
- systemd service is not ran as the created user [\#436](https://github.com/sbt/sbt-native-packager/issues/436)

**Merged pull requests:**

- Release preparations [\#550](https://github.com/sbt/sbt-native-packager/pull/550) ([muuki88](https://github.com/muuki88))
- Fix warnings from rpm lint [\#547](https://github.com/sbt/sbt-native-packager/pull/547) ([dwhjames](https://github.com/dwhjames))
- Fixing docs with newest changes [\#546](https://github.com/sbt/sbt-native-packager/pull/546) ([muuki88](https://github.com/muuki88))
- fix for issue \#529 [\#544](https://github.com/sbt/sbt-native-packager/pull/544) ([dwhjames](https://github.com/dwhjames))
- Optimize Travis script [\#543](https://github.com/sbt/sbt-native-packager/pull/543) ([dwhjames](https://github.com/dwhjames))
- A Vagrant+Ansible automated test project  [\#542](https://github.com/sbt/sbt-native-packager/pull/542) ([dwhjames](https://github.com/dwhjames))
- fix SystemV init script template for rpm packaging [\#541](https://github.com/sbt/sbt-native-packager/pull/541) ([dwhjames](https://github.com/dwhjames))
- test added for daemon user in systemd \#436 [\#540](https://github.com/sbt/sbt-native-packager/pull/540) ([kardapoltsev](https://github.com/kardapoltsev))
- make use of stopService loader function in rpm preun template [\#539](https://github.com/sbt/sbt-native-packager/pull/539) ([dwhjames](https://github.com/dwhjames))
- expand the doc for the start script template vars [\#538](https://github.com/sbt/sbt-native-packager/pull/538) ([dwhjames](https://github.com/dwhjames))
- fix use of app\_name and exec vars in systemv start-rpm-template [\#537](https://github.com/sbt/sbt-native-packager/pull/537) ([dwhjames](https://github.com/dwhjames))
- Trying travis-ci container architecture for faster builds. [\#454](https://github.com/sbt/sbt-native-packager/pull/454) ([muuki88](https://github.com/muuki88))

## [v1.0.0-RC2](https://github.com/sbt/sbt-native-packager/tree/v1.0.0-RC2) (2015-03-29)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.0.0-RC1...v1.0.0-RC2)

**Fixed bugs:**

- sysvinit: Stop does not remove invalid PID file [\#531](https://github.com/sbt/sbt-native-packager/issues/531)

**Closed issues:**

- Docker - Setting Daemon user fails - not in right order [\#530](https://github.com/sbt/sbt-native-packager/issues/530)
- Build.scala instructions not working [\#527](https://github.com/sbt/sbt-native-packager/issues/527)
- Docker - Passing production.conf [\#522](https://github.com/sbt/sbt-native-packager/issues/522)
- Multiple instances of Play application [\#520](https://github.com/sbt/sbt-native-packager/issues/520)
- Changing the default installation directory [\#506](https://github.com/sbt/sbt-native-packager/issues/506)
- Unable to override akka-bash-template [\#426](https://github.com/sbt/sbt-native-packager/issues/426)
- packageName in Docker not found [\#413](https://github.com/sbt/sbt-native-packager/issues/413)
- Can't combine PlayJava with JDebPackaging plugin [\#407](https://github.com/sbt/sbt-native-packager/issues/407)
- Specify default memory parameters of java\_application BASH script in the build [\#82](https://github.com/sbt/sbt-native-packager/issues/82)

**Merged pull requests:**

- Improvements to RPM documentation [\#535](https://github.com/sbt/sbt-native-packager/pull/535) ([dwhjames](https://github.com/dwhjames))
- Fix and improve docs for rpmAutoreq and rpmAutoprov [\#534](https://github.com/sbt/sbt-native-packager/pull/534) ([dwhjames](https://github.com/dwhjames))
- !Update default dockerBaseImage to official repo [\#533](https://github.com/sbt/sbt-native-packager/pull/533) ([danielwegener](https://github.com/danielwegener))
- Fix \#531: Simplify stop\(\) function on Debian based systems. [\#532](https://github.com/sbt/sbt-native-packager/pull/532) ([fabiankrack](https://github.com/fabiankrack))
- Codacy fixes [\#528](https://github.com/sbt/sbt-native-packager/pull/528) ([myyk](https://github.com/myyk))
- Added support for environment configuration file. [\#526](https://github.com/sbt/sbt-native-packager/pull/526) ([knshiro](https://github.com/knshiro))
- Revert "Codacy recommended cleanup." [\#525](https://github.com/sbt/sbt-native-packager/pull/525) ([muuki88](https://github.com/muuki88))
- Codacy recommended cleanup. [\#524](https://github.com/sbt/sbt-native-packager/pull/524) ([myyk](https://github.com/myyk))
- Fix incorrect information in RPM Plugin docs. [\#523](https://github.com/sbt/sbt-native-packager/pull/523) ([myyk](https://github.com/myyk))
- Fix typos in the documentation [\#519](https://github.com/sbt/sbt-native-packager/pull/519) ([jonas](https://github.com/jonas))
- Fix typo 'scirpt' to be 'script' [\#517](https://github.com/sbt/sbt-native-packager/pull/517) ([myyk](https://github.com/myyk))
- Added launcher jar plugin [\#514](https://github.com/sbt/sbt-native-packager/pull/514) ([jroper](https://github.com/jroper))
- Basic image generation detection test for JDKPackagerPlugin. [\#512](https://github.com/sbt/sbt-native-packager/pull/512) ([metasim](https://github.com/metasim))
- FIX \#82 adding the ability to specify jvm options via sbt [\#510](https://github.com/sbt/sbt-native-packager/pull/510) ([muuki88](https://github.com/muuki88))
- fix appveyor test  [\#507](https://github.com/sbt/sbt-native-packager/pull/507) ([nazoking](https://github.com/nazoking))
- FIX \#502 Removing JAVA\_OPTS [\#503](https://github.com/sbt/sbt-native-packager/pull/503) ([muuki88](https://github.com/muuki88))

## [v1.0.0-RC1](https://github.com/sbt/sbt-native-packager/tree/v1.0.0-RC1) (2015-02-24)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.0.0-M5...v1.0.0-RC1)

**Fixed bugs:**

- Default value for `jdkPackagerTool` is computed incorrectly on Windows. [\#495](https://github.com/sbt/sbt-native-packager/issues/495)
- bashScriptConfigLocation in rpm:packageBin via 1.0.0-M4 [\#482](https://github.com/sbt/sbt-native-packager/issues/482)
- Permission denied to execute entry point in Docker [\#331](https://github.com/sbt/sbt-native-packager/issues/331)

**Closed issues:**

- Failing Windows Tests [\#502](https://github.com/sbt/sbt-native-packager/issues/502)
- Clean image after publishing [\#500](https://github.com/sbt/sbt-native-packager/issues/500)
- UnsupportedOperationException with play 2.3.7, sbt-np 1.0.0-M5 and jdeb on windows 7 [\#499](https://github.com/sbt/sbt-native-packager/issues/499)
- Document unsupport for Java 6 [\#498](https://github.com/sbt/sbt-native-packager/issues/498)
- Created Volumes in Docker plugin, are not owned by daemon. [\#485](https://github.com/sbt/sbt-native-packager/issues/485)
- Evaluate appveyor for windows tests [\#466](https://github.com/sbt/sbt-native-packager/issues/466)
- Docker Refactoring - Parent Issue [\#453](https://github.com/sbt/sbt-native-packager/issues/453)
- Optionally inherit docker entrypoint, and cmd [\#433](https://github.com/sbt/sbt-native-packager/issues/433)
- Add command in Dockerfile [\#385](https://github.com/sbt/sbt-native-packager/issues/385)

**Merged pull requests:**

- FIX \#482 simple error, nasty solution. Works for the moment, but needs [\#505](https://github.com/sbt/sbt-native-packager/pull/505) ([muuki88](https://github.com/muuki88))
- Fix \#495. Enhanced `locateJDKPackagerTool` to work on Windows when SBT... [\#496](https://github.com/sbt/sbt-native-packager/pull/496) ([metasim](https://github.com/metasim))
- Added translation of `jvmopts` contents and enhanced example app. [\#494](https://github.com/sbt/sbt-native-packager/pull/494) ([metasim](https://github.com/metasim))
- Wip/appveyor windows tests [\#493](https://github.com/sbt/sbt-native-packager/pull/493) ([muuki88](https://github.com/muuki88))

## [v1.0.0-M5](https://github.com/sbt/sbt-native-packager/tree/v1.0.0-M5) (2015-02-15)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.8.0...v1.0.0-M5)

**Fixed bugs:**

- Error i documentation "packageArchetype.java\_app" [\#489](https://github.com/sbt/sbt-native-packager/issues/489)
- \[doc\] "Generating files for the package" examples not rendered [\#469](https://github.com/sbt/sbt-native-packager/issues/469)
- Revert "\[fix \#472\] /etc/default/\<package-name\> should be shell script se... [\#491](https://github.com/sbt/sbt-native-packager/pull/491) ([muuki88](https://github.com/muuki88))
- FIX \#469 missing colons [\#470](https://github.com/sbt/sbt-native-packager/pull/470) ([muuki88](https://github.com/muuki88))

**Closed issues:**

- multi module project is packaged into multiple docker images [\#480](https://github.com/sbt/sbt-native-packager/issues/480)
- Using the -jvm-debug flag should pass the -agentlib jvm flag instead of -Xrunjdwp flag [\#476](https://github.com/sbt/sbt-native-packager/issues/476)
- sbt-release with docker plugin step [\#475](https://github.com/sbt/sbt-native-packager/issues/475)
- "/etc/default/\<package-name\>" should be shell script setting envars [\#472](https://github.com/sbt/sbt-native-packager/issues/472)
- 0.8.0: JDebPackaging doesn't take into account daemonUser setting [\#468](https://github.com/sbt/sbt-native-packager/issues/468)
- Docker file use wrapping pre/post scripting for the JVM process [\#461](https://github.com/sbt/sbt-native-packager/issues/461)
- Typo in docs [\#460](https://github.com/sbt/sbt-native-packager/issues/460)
- Warning when using 0.8.0 with Play 2.3.7 [\#459](https://github.com/sbt/sbt-native-packager/issues/459)
- Ability to override entrypoint when building docker containers [\#410](https://github.com/sbt/sbt-native-packager/issues/410)
- allow archive targets to eliminate top-level directory [\#276](https://github.com/sbt/sbt-native-packager/issues/276)

**Merged pull requests:**

- Experimental support for formats generated by the JDK 8 `javapackager` tool. [\#492](https://github.com/sbt/sbt-native-packager/pull/492) ([metasim](https://github.com/metasim))
- FIX \#489: Small fix in documentation [\#490](https://github.com/sbt/sbt-native-packager/pull/490) ([kardapoltsev](https://github.com/kardapoltsev))
- Upgrading to java 7 and using posix nio API [\#487](https://github.com/sbt/sbt-native-packager/pull/487) ([muuki88](https://github.com/muuki88))
- Wip/docker refactoring [\#486](https://github.com/sbt/sbt-native-packager/pull/486) ([muuki88](https://github.com/muuki88))
- Add a Gitter chat badge to README.md [\#483](https://github.com/sbt/sbt-native-packager/pull/483) ([gitter-badger](https://github.com/gitter-badger))
- \[Fix \#476\] Fixing -jvm-debug flags so that they use args compatible with the latest JDKs [\#478](https://github.com/sbt/sbt-native-packager/pull/478) ([jateenjoshi](https://github.com/jateenjoshi))
- Fix broken link to Docker help. [\#474](https://github.com/sbt/sbt-native-packager/pull/474) ([matthughes](https://github.com/matthughes))
- \[fix \#472\] /etc/default/\<package-name\> should be shell script setting envars [\#473](https://github.com/sbt/sbt-native-packager/pull/473) ([dhardy92](https://github.com/dhardy92))
- Added missing statement in archetypes doc [\#464](https://github.com/sbt/sbt-native-packager/pull/464) ([tartakynov](https://github.com/tartakynov))
- Docker maintainer is no longer required [\#463](https://github.com/sbt/sbt-native-packager/pull/463) ([benmccann](https://github.com/benmccann))
- wait for process to stop or timeout in systemv init script [\#462](https://github.com/sbt/sbt-native-packager/pull/462) ([dhardy92](https://github.com/dhardy92))

## [v0.8.0](https://github.com/sbt/sbt-native-packager/tree/v0.8.0) (2015-01-08)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.0.0-M4...v0.8.0)

**Fixed bugs:**

- SystemV init script is not idempotent [\#451](https://github.com/sbt/sbt-native-packager/issues/451)

**Merged pull requests:**

- windows bat exit code fix [\#423](https://github.com/sbt/sbt-native-packager/pull/423) ([bjuric](https://github.com/bjuric))

## [v1.0.0-M4](https://github.com/sbt/sbt-native-packager/tree/v1.0.0-M4) (2015-01-08)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.8.0-RC2...v1.0.0-M4)

**Fixed bugs:**

- RPMs cannot be uninstalled [\#435](https://github.com/sbt/sbt-native-packager/issues/435)
- Windows bat does not return exit code 1 when java app returns non zero exit code [\#420](https://github.com/sbt/sbt-native-packager/issues/420)
- control directory has bad permissions 750 \(must be \>=0755 and \<=0775\) [\#419](https://github.com/sbt/sbt-native-packager/issues/419)
- Has Java 7 happened already? [\#416](https://github.com/sbt/sbt-native-packager/issues/416)
- Back to specific directory for ADD command [\#458](https://github.com/sbt/sbt-native-packager/pull/458) ([huntc](https://github.com/huntc))
- Using `--startas` instead of `--exec` now in order to make start script idempotent. [\#457](https://github.com/sbt/sbt-native-packager/pull/457) ([OlegIlyenko](https://github.com/OlegIlyenko))
- Run the systemd service as the created & specified daemon user [\#441](https://github.com/sbt/sbt-native-packager/pull/441) ([nefilim](https://github.com/nefilim))
- Fix/windows bat script [\#440](https://github.com/sbt/sbt-native-packager/pull/440) ([muuki88](https://github.com/muuki88))
- FIX install command in upstart to use daemon\_group [\#434](https://github.com/sbt/sbt-native-packager/pull/434) ([muuki88](https://github.com/muuki88))
- Docker mappings - incomplete? [\#424](https://github.com/sbt/sbt-native-packager/pull/424) ([huntc](https://github.com/huntc))

**Closed issues:**

- dockerUpdateLatest does not work with Docker 1.4.1 [\#456](https://github.com/sbt/sbt-native-packager/issues/456)
- uid and gid for linux dist [\#447](https://github.com/sbt/sbt-native-packager/issues/447)
- expose RPM file created [\#445](https://github.com/sbt/sbt-native-packager/issues/445)
- Update documentation [\#443](https://github.com/sbt/sbt-native-packager/issues/443)
- Update versions on README? [\#432](https://github.com/sbt/sbt-native-packager/issues/432)
- error: reference to Debian is ambiguous; [\#428](https://github.com/sbt/sbt-native-packager/issues/428)
- Should we be distributing docker images or just DockerFile with its files as we do now? [\#425](https://github.com/sbt/sbt-native-packager/issues/425)
- custom mainClass not working on Windows [\#415](https://github.com/sbt/sbt-native-packager/issues/415)
- Custom configuration is replaced with the default one during package install [\#378](https://github.com/sbt/sbt-native-packager/issues/378)

**Merged pull requests:**

- Fixing update-latest to work with docker 1.3 and greater [\#452](https://github.com/sbt/sbt-native-packager/pull/452) ([muuki88](https://github.com/muuki88))
- fixes a typo leading to bad replacement [\#450](https://github.com/sbt/sbt-native-packager/pull/450) ([roboll](https://github.com/roboll))
- add support for uid and gid [\#449](https://github.com/sbt/sbt-native-packager/pull/449) ([roboll](https://github.com/roboll))
- Stipulation of docker version in documentation [\#448](https://github.com/sbt/sbt-native-packager/pull/448) ([huntc](https://github.com/huntc))
- Replace bash image with java’s in akka-bash-template [\#446](https://github.com/sbt/sbt-native-packager/pull/446) ([michalrus](https://github.com/michalrus))
- FIX \#443 Refactor documentation. Work in progress [\#444](https://github.com/sbt/sbt-native-packager/pull/444) ([muuki88](https://github.com/muuki88))
- \#415 custom mainclass for Windows [\#442](https://github.com/sbt/sbt-native-packager/pull/442) ([tartakynov](https://github.com/tartakynov))
- FIX \#435 Adding loader and control functions [\#439](https://github.com/sbt/sbt-native-packager/pull/439) ([muuki88](https://github.com/muuki88))
- Made -agentlib options before the classpath in the bash script. [\#431](https://github.com/sbt/sbt-native-packager/pull/431) ([ht290](https://github.com/ht290))
- Add the required import. [\#430](https://github.com/sbt/sbt-native-packager/pull/430) ([apenney](https://github.com/apenney))
- Fix typo in debian:packageBin. [\#429](https://github.com/sbt/sbt-native-packager/pull/429) ([apenney](https://github.com/apenney))
- Add documentation about Play 2 packaging as deb- and rpm-packages [\#427](https://github.com/sbt/sbt-native-packager/pull/427) ([artempyanykh](https://github.com/artempyanykh))

## [v0.8.0-RC2](https://github.com/sbt/sbt-native-packager/tree/v0.8.0-RC2) (2014-11-26)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.7.7-RC1...v0.8.0-RC2)

## [v0.7.7-RC1](https://github.com/sbt/sbt-native-packager/tree/v0.7.7-RC1) (2014-11-26)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.0.0-M3...v0.7.7-RC1)

## [v1.0.0-M3](https://github.com/sbt/sbt-native-packager/tree/v1.0.0-M3) (2014-11-26)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.0.0-M2...v1.0.0-M3)

**Fixed bugs:**

- Script replacements doesn't work with jdeb packaging [\#405](https://github.com/sbt/sbt-native-packager/issues/405)
- Start command too long on some platforms with big classpaths [\#72](https://github.com/sbt/sbt-native-packager/issues/72)

**Closed issues:**

- Cannot publish to http \(or self-signed https\) docker registry [\#412](https://github.com/sbt/sbt-native-packager/issues/412)
- Cannot pass dashed script options on windows [\#409](https://github.com/sbt/sbt-native-packager/issues/409)
- Cannot pack `templates/etc-default` into Docker container [\#398](https://github.com/sbt/sbt-native-packager/issues/398)
- Publish universal tgz artifact only [\#349](https://github.com/sbt/sbt-native-packager/issues/349)
- Ability to add to class path in the bash script [\#124](https://github.com/sbt/sbt-native-packager/issues/124)

**Merged pull requests:**

- Better Entrypoint Support [\#411](https://github.com/sbt/sbt-native-packager/pull/411) ([mhamrah](https://github.com/mhamrah))
- FIX \#394 adding tests for native packaging with 0440 permissions [\#408](https://github.com/sbt/sbt-native-packager/pull/408) ([muuki88](https://github.com/muuki88))
- FIX \#405: Script replacements doesn't work with jdeb packaging [\#406](https://github.com/sbt/sbt-native-packager/pull/406) ([kardapoltsev](https://github.com/kardapoltsev))
- Fix typo contstruct -\> construct. [\#404](https://github.com/sbt/sbt-native-packager/pull/404) ([falmp](https://github.com/falmp))
- fix too long classpaths in script\(bat/bash\). [\#397](https://github.com/sbt/sbt-native-packager/pull/397) ([nazoking](https://github.com/nazoking))
- fix windows batch argument probrem. [\#393](https://github.com/sbt/sbt-native-packager/pull/393) ([nazoking](https://github.com/nazoking))

## [v1.0.0-M2](https://github.com/sbt/sbt-native-packager/tree/v1.0.0-M2) (2014-11-07)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v1.0.0-M1...v1.0.0-M2)

**Fixed bugs:**

- server deb packages are not lintian clean [\#391](https://github.com/sbt/sbt-native-packager/issues/391)
- Can't create .deb files with jdeb on windows [\#388](https://github.com/sbt/sbt-native-packager/issues/388)
- Upstart not working on Ubuntu 14.04: cannot create PID file [\#357](https://github.com/sbt/sbt-native-packager/issues/357)
- Play backward compatibility issue [\#403](https://github.com/sbt/sbt-native-packager/pull/403) ([huntc](https://github.com/huntc))

**Closed issues:**

- Document using the autoplugins version in a .scala build [\#402](https://github.com/sbt/sbt-native-packager/issues/402)

**Merged pull requests:**

- Not hardcoding bashscript config location \#398 [\#401](https://github.com/sbt/sbt-native-packager/pull/401) ([muuki88](https://github.com/muuki88))
- FIX \#391 Fixing all lintian errors and some warnings [\#400](https://github.com/sbt/sbt-native-packager/pull/400) ([muuki88](https://github.com/muuki88))
- FIX \#388 remove dependencies to chmod. [\#399](https://github.com/sbt/sbt-native-packager/pull/399) ([muuki88](https://github.com/muuki88))
- Relocated the scope for common docker settings [\#392](https://github.com/sbt/sbt-native-packager/pull/392) ([huntc](https://github.com/huntc))
- Setting maintainer for Docker images can be optional. [\#390](https://github.com/sbt/sbt-native-packager/pull/390) ([fiadliel](https://github.com/fiadliel))
- Remove AutoPlugin triggers from plugins. [\#389](https://github.com/sbt/sbt-native-packager/pull/389) ([fiadliel](https://github.com/fiadliel))
- Update README.md [\#387](https://github.com/sbt/sbt-native-packager/pull/387) ([levinotik](https://github.com/levinotik))

## [v1.0.0-M1](https://github.com/sbt/sbt-native-packager/tree/v1.0.0-M1) (2014-10-22)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.8.0-RC1...v1.0.0-M1)

**Merged pull requests:**

- Fix example code in GettingStartedServers/MyFirstProject docs. [\#383](https://github.com/sbt/sbt-native-packager/pull/383) ([artempyanykh](https://github.com/artempyanykh))
- Fix permissions and group for /var/run/${{app\_name}} folder in SystemV init script [\#382](https://github.com/sbt/sbt-native-packager/pull/382) ([artempyanykh](https://github.com/artempyanykh))
- Describe server loading options in server docs [\#379](https://github.com/sbt/sbt-native-packager/pull/379) ([mhamrah](https://github.com/mhamrah))
- Wip/autoplugins [\#374](https://github.com/sbt/sbt-native-packager/pull/374) ([muuki88](https://github.com/muuki88))

## [v0.8.0-RC1](https://github.com/sbt/sbt-native-packager/tree/v0.8.0-RC1) (2014-10-21)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.7.6...v0.8.0-RC1)

**Merged pull requests:**

- FIX \#372: On upgrade service is stopped twice [\#373](https://github.com/sbt/sbt-native-packager/pull/373) ([kardapoltsev](https://github.com/kardapoltsev))
- fix typo in mappings example [\#371](https://github.com/sbt/sbt-native-packager/pull/371) ([f0y](https://github.com/f0y))
- Handles OpenJDK version output on Windows [\#369](https://github.com/sbt/sbt-native-packager/pull/369) ([henrikengstrom](https://github.com/henrikengstrom))

## [v0.7.6](https://github.com/sbt/sbt-native-packager/tree/v0.7.6) (2014-10-21)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.8.0-M2...v0.7.6)

**Fixed bugs:**

- Incorrect group for /var/run/${{app\_name}} folder with SystemV init-scripts. [\#381](https://github.com/sbt/sbt-native-packager/issues/381)
- On upgrade service is stopped twice [\#372](https://github.com/sbt/sbt-native-packager/issues/372)
- 0.8.0-M2 artifact missing on repo.scala-sbt.org [\#370](https://github.com/sbt/sbt-native-packager/issues/370)
- Detect OpenJDK on Windows [\#353](https://github.com/sbt/sbt-native-packager/issues/353)

**Closed issues:**

- Custom bash script to run before default start script [\#380](https://github.com/sbt/sbt-native-packager/issues/380)
- Stale PID file prevents service from starting with Upstart [\#377](https://github.com/sbt/sbt-native-packager/issues/377)
- Easy way to use upstart with rpm? [\#376](https://github.com/sbt/sbt-native-packager/issues/376)
- Ability to add libsigar \(and arch dependent packages\) to RPM Build [\#375](https://github.com/sbt/sbt-native-packager/issues/375)
- Wrong file mask [\#367](https://github.com/sbt/sbt-native-packager/issues/367)

## [v0.8.0-M2](https://github.com/sbt/sbt-native-packager/tree/v0.8.0-M2) (2014-10-01)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.7.5...v0.8.0-M2)

**Fixed bugs:**

- changed template for /etc/init/{{app\_name}} This seems to fix issue \#357... [\#358](https://github.com/sbt/sbt-native-packager/pull/358) ([flowma](https://github.com/flowma))

**Merged pull requests:**

- Preparing documentation for next release [\#366](https://github.com/sbt/sbt-native-packager/pull/366) ([muuki88](https://github.com/muuki88))
- archetype for Akka microKernel application \#316  [\#363](https://github.com/sbt/sbt-native-packager/pull/363) ([c4po](https://github.com/c4po))
- FIX \#295 Adding documentation for docker tags [\#360](https://github.com/sbt/sbt-native-packager/pull/360) ([muuki88](https://github.com/muuki88))
- FIX \#318 Wildcard match on errorcode [\#359](https://github.com/sbt/sbt-native-packager/pull/359) ([muuki88](https://github.com/muuki88))
- Improved docs for packageMappings in Universal and Linux [\#356](https://github.com/sbt/sbt-native-packager/pull/356) ([muuki88](https://github.com/muuki88))
- Permissions for /etc/default/app\_name changed to 644 [\#354](https://github.com/sbt/sbt-native-packager/pull/354) ([kardapoltsev](https://github.com/kardapoltsev))
- Fix typos in getting started docs. [\#351](https://github.com/sbt/sbt-native-packager/pull/351) ([artempyanykh](https://github.com/artempyanykh))
- Fix typo in docs about 'MyFirstProject' [\#350](https://github.com/sbt/sbt-native-packager/pull/350) ([artempyanykh](https://github.com/artempyanykh))
- Wip/bashscript refactoring [\#348](https://github.com/sbt/sbt-native-packager/pull/348) ([muuki88](https://github.com/muuki88))
- Adding documentation on how to deploy packages [\#344](https://github.com/sbt/sbt-native-packager/pull/344) ([muuki88](https://github.com/muuki88))
- \[docker\] Java Latest [\#343](https://github.com/sbt/sbt-native-packager/pull/343) ([rfranco](https://github.com/rfranco))

## [v0.7.5](https://github.com/sbt/sbt-native-packager/tree/v0.7.5) (2014-10-01)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.7.5-RC2...v0.7.5)

**Fixed bugs:**

- Usage with Play 2.3: Assets are not part of application artifact [\#362](https://github.com/sbt/sbt-native-packager/issues/362)
- Build failed on OS X [\#327](https://github.com/sbt/sbt-native-packager/issues/327)
- scala.MatchError: 139 \(of class java.lang.Integer\) in RpmHelper:87 [\#318](https://github.com/sbt/sbt-native-packager/issues/318)
- Version appears twice in artefact in publish task [\#270](https://github.com/sbt/sbt-native-packager/issues/270)
- Version 0.7.1 and onwards does not build RPMs on Mac OSX Host [\#266](https://github.com/sbt/sbt-native-packager/issues/266)
- Config in etc-default has wrong permissions [\#264](https://github.com/sbt/sbt-native-packager/issues/264)

**Closed issues:**

- Dockerfile does not include EXPOSE command [\#364](https://github.com/sbt/sbt-native-packager/issues/364)
- Modifying /etc/app/application.conf not taken in account after restart [\#361](https://github.com/sbt/sbt-native-packager/issues/361)
- Support signing debs during the build/publish process [\#345](https://github.com/sbt/sbt-native-packager/issues/345)
- jdeb .debs missing /etc/default/$name and /etc/init.d/$name files [\#342](https://github.com/sbt/sbt-native-packager/issues/342)
- archetype for akka Microkernel app? [\#316](https://github.com/sbt/sbt-native-packager/issues/316)
- Building blocks for custom package formats? [\#312](https://github.com/sbt/sbt-native-packager/issues/312)
- \[docker\] name for build tag [\#295](https://github.com/sbt/sbt-native-packager/issues/295)
- Remapping /etc/\<app\> \<-\> /usr/share/\<app\>/conf symlink [\#287](https://github.com/sbt/sbt-native-packager/issues/287)
- Ability to assembly fat jar rather than put all dependent jars into lib-directory [\#285](https://github.com/sbt/sbt-native-packager/issues/285)
- .deb creation is really slow [\#135](https://github.com/sbt/sbt-native-packager/issues/135)
- rpm and \[error\] logging [\#103](https://github.com/sbt/sbt-native-packager/issues/103)
- support multiple main classes in a single module [\#80](https://github.com/sbt/sbt-native-packager/issues/80)
- Provide docs how to use the plugin with multi-project projects [\#52](https://github.com/sbt/sbt-native-packager/issues/52)

## [v0.7.5-RC2](https://github.com/sbt/sbt-native-packager/tree/v0.7.5-RC2) (2014-08-31)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.8.0-M1...v0.7.5-RC2)

## [v0.8.0-M1](https://github.com/sbt/sbt-native-packager/tree/v0.8.0-M1) (2014-08-31)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.7.5-RC1...v0.8.0-M1)

**Fixed bugs:**

- Fixed wrong archive name in jdeb packaging [\#328](https://github.com/sbt/sbt-native-packager/pull/328) ([kardapoltsev](https://github.com/kardapoltsev))

**Closed issues:**

- syntax error in startup script generated by 'activator dist' in play-2.3.3 [\#336](https://github.com/sbt/sbt-native-packager/issues/336)
- rpm:packageBin needs License [\#335](https://github.com/sbt/sbt-native-packager/issues/335)
- Add the changelog on the RPM [\#330](https://github.com/sbt/sbt-native-packager/issues/330)

**Merged pull requests:**

- fix minor spelling mistakes [\#338](https://github.com/sbt/sbt-native-packager/pull/338) ([mmorearty](https://github.com/mmorearty))
- change plugin version to 0.7.5-RC1 in documentation  [\#337](https://github.com/sbt/sbt-native-packager/pull/337) ([aviks](https://github.com/aviks))
- Added support for classifiers in multiproject builds [\#333](https://github.com/sbt/sbt-native-packager/pull/333) ([jroper](https://github.com/jroper))
- Implemented the changelog to RPM [\#332](https://github.com/sbt/sbt-native-packager/pull/332) ([antonini](https://github.com/antonini))
- Fix RPM System User Deletion on Upgrade [\#326](https://github.com/sbt/sbt-native-packager/pull/326) ([pcting](https://github.com/pcting))
- FIX \#324 adding empty dirs to deb package [\#325](https://github.com/sbt/sbt-native-packager/pull/325) ([kardapoltsev](https://github.com/kardapoltsev))
- FIX \#322 adding permissions to jdeb packaging [\#323](https://github.com/sbt/sbt-native-packager/pull/323) ([kardapoltsev](https://github.com/kardapoltsev))
- maintainer should be in Docker or Dockerfile will write an empty value [\#321](https://github.com/sbt/sbt-native-packager/pull/321) ([treyhyde](https://github.com/treyhyde))
- Custom mainclass [\#319](https://github.com/sbt/sbt-native-packager/pull/319) ([jkutner](https://github.com/jkutner))
- flag to update latest tag [\#317](https://github.com/sbt/sbt-native-packager/pull/317) ([rfranco](https://github.com/rfranco))
- No need for import [\#315](https://github.com/sbt/sbt-native-packager/pull/315) ([jaceklaskowski](https://github.com/jaceklaskowski))
- FIX \#304 adding requirements for debian packaging [\#311](https://github.com/sbt/sbt-native-packager/pull/311) ([muuki88](https://github.com/muuki88))
- Adding docker tests to release script [\#308](https://github.com/sbt/sbt-native-packager/pull/308) ([muuki88](https://github.com/muuki88))
- Upgrade to jdeb 1.3 to stop old version of slf4j from being pulled in [\#307](https://github.com/sbt/sbt-native-packager/pull/307) ([benmccann](https://github.com/benmccann))
- FIX \#217 Introduced executableScriptName setting [\#306](https://github.com/sbt/sbt-native-packager/pull/306) ([muuki88](https://github.com/muuki88))
- Remove cross-build configuration from build.sbt. [\#305](https://github.com/sbt/sbt-native-packager/pull/305) ([fiadliel](https://github.com/fiadliel))
- Removing sbt 0.12.x support [\#303](https://github.com/sbt/sbt-native-packager/pull/303) ([muuki88](https://github.com/muuki88))
- Update README now that 0.7.2 is released [\#300](https://github.com/sbt/sbt-native-packager/pull/300) ([benmccann](https://github.com/benmccann))
- Adding docs for name-delegation in \#250 [\#299](https://github.com/sbt/sbt-native-packager/pull/299) ([muuki88](https://github.com/muuki88))
- Fixes \#292 - Not feeding new isSnapshot value to ivy configuration. [\#298](https://github.com/sbt/sbt-native-packager/pull/298) ([jsuereth](https://github.com/jsuereth))
- Clean up name delegation in the plugin.  A few rules: [\#250](https://github.com/sbt/sbt-native-packager/pull/250) ([jsuereth](https://github.com/jsuereth))

## [v0.7.5-RC1](https://github.com/sbt/sbt-native-packager/tree/v0.7.5-RC1) (2014-08-20)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.7.4...v0.7.5-RC1)

**Fixed bugs:**

- Executable flag is not set when using jDeb packaging [\#322](https://github.com/sbt/sbt-native-packager/issues/322)
- docker:stage problem - no application is built [\#314](https://github.com/sbt/sbt-native-packager/issues/314)
- Docker Build Fail [\#310](https://github.com/sbt/sbt-native-packager/issues/310)
- old slf4j pulled by jdeb breaks sbt-web asset pipeline  [\#291](https://github.com/sbt/sbt-native-packager/issues/291)

**Closed issues:**

- Jdeb doesn't package empty directories [\#324](https://github.com/sbt/sbt-native-packager/issues/324)
- CentOS Rpm Group field must be present in package: \(main package\) [\#309](https://github.com/sbt/sbt-native-packager/issues/309)
- Debian packaging error message is not great [\#304](https://github.com/sbt/sbt-native-packager/issues/304)

## [v0.7.4](https://github.com/sbt/sbt-native-packager/tree/v0.7.4) (2014-07-28)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.7.3...v0.7.4)

**Closed issues:**

- Ability to control bash / bat file name other than via normalizedName [\#217](https://github.com/sbt/sbt-native-packager/issues/217)

## [v0.7.3](https://github.com/sbt/sbt-native-packager/tree/v0.7.3) (2014-07-21)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.7.2...v0.7.3)

**Fixed bugs:**

- Docker Commands output in wrong order? [\#297](https://github.com/sbt/sbt-native-packager/issues/297)
- Cannot publish snapshots using universal:publish [\#292](https://github.com/sbt/sbt-native-packager/issues/292)
- "name in Linux := " ignored [\#188](https://github.com/sbt/sbt-native-packager/issues/188)

**Closed issues:**

- Docker docs are conflicting [\#301](https://github.com/sbt/sbt-native-packager/issues/301)

**Merged pull requests:**

- \[docker\] add build --force-rm [\#294](https://github.com/sbt/sbt-native-packager/pull/294) ([rfranco](https://github.com/rfranco))
- fix order of docker commands [\#293](https://github.com/sbt/sbt-native-packager/pull/293) ([rfranco](https://github.com/rfranco))

## [v0.7.2](https://github.com/sbt/sbt-native-packager/tree/v0.7.2) (2014-07-10)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.7.2-RC2...v0.7.2)

**Fixed bugs:**

- Use StringUtilities.normalize to allow compilation with SBT 0.12. [\#288](https://github.com/sbt/sbt-native-packager/pull/288) ([fiadliel](https://github.com/fiadliel))

**Closed issues:**

- log directory symlink location differs between installed package and documentation [\#282](https://github.com/sbt/sbt-native-packager/issues/282)
- Support for SBT 0.13.5-M2 [\#243](https://github.com/sbt/sbt-native-packager/issues/243)
- Review play init.d script from blog post [\#181](https://github.com/sbt/sbt-native-packager/issues/181)

**Merged pull requests:**

- Adding documentation for JDeb Packaging [\#290](https://github.com/sbt/sbt-native-packager/pull/290) ([muuki88](https://github.com/muuki88))
- Upgrading sbt and adding releaseNotes plugin [\#286](https://github.com/sbt/sbt-native-packager/pull/286) ([muuki88](https://github.com/muuki88))
- First refactoring to provide multiple debian packaging strategies [\#284](https://github.com/sbt/sbt-native-packager/pull/284) ([muuki88](https://github.com/muuki88))
- - Make the /var/log/\<pkg\> symlink doc match the package location. [\#283](https://github.com/sbt/sbt-native-packager/pull/283) ([paddymahoney](https://github.com/paddymahoney))
- Documentation for docker support. [\#281](https://github.com/sbt/sbt-native-packager/pull/281) ([fiadliel](https://github.com/fiadliel))
- Support Docker volumes. [\#280](https://github.com/sbt/sbt-native-packager/pull/280) ([fiadliel](https://github.com/fiadliel))
- Allow name in Docker to be modified. [\#279](https://github.com/sbt/sbt-native-packager/pull/279) ([fiadliel](https://github.com/fiadliel))
- Support EXPOSE \[port\] ... for Docker [\#278](https://github.com/sbt/sbt-native-packager/pull/278) ([fiadliel](https://github.com/fiadliel))
- Support building and publishing Docker images. [\#277](https://github.com/sbt/sbt-native-packager/pull/277) ([fiadliel](https://github.com/fiadliel))

## [v0.7.2-RC2](https://github.com/sbt/sbt-native-packager/tree/v0.7.2-RC2) (2014-06-21)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.7.2-RC1...v0.7.2-RC2)

**Fixed bugs:**

- 'die' command not found in bash script [\#259](https://github.com/sbt/sbt-native-packager/issues/259)
- Fix java 8 version check [\#260](https://github.com/sbt/sbt-native-packager/pull/260) ([mpilquist](https://github.com/mpilquist))

**Closed issues:**

- service start on Centos produces ENOTTY error [\#275](https://github.com/sbt/sbt-native-packager/issues/275)
- systemv start-rpm-template redirects daemon's output to /dev/null [\#273](https://github.com/sbt/sbt-native-packager/issues/273)
- Add systemd support for java server applications [\#249](https://github.com/sbt/sbt-native-packager/issues/249)
- Document imports [\#247](https://github.com/sbt/sbt-native-packager/issues/247)

**Merged pull requests:**

- Switched systemv start-rpm-template daemon's output [\#274](https://github.com/sbt/sbt-native-packager/pull/274) ([spigene](https://github.com/spigene))
- Fixed user deletion issue in rpm postuninstall scriptlet. [\#272](https://github.com/sbt/sbt-native-packager/pull/272) ([spigene](https://github.com/spigene))
- implemented `die` function in bash-template [\#271](https://github.com/sbt/sbt-native-packager/pull/271) ([philipjkim](https://github.com/philipjkim))
- Support for changelog and .changes file generation [\#268](https://github.com/sbt/sbt-native-packager/pull/268) ([jaunis](https://github.com/jaunis))
- Let bash-template work when the directory contains spaces. [\#267](https://github.com/sbt/sbt-native-packager/pull/267) ([darabos](https://github.com/darabos))
- Adding experimental osx travis-ci support [\#257](https://github.com/sbt/sbt-native-packager/pull/257) ([muuki88](https://github.com/muuki88))
- FIX \#249. Systemd support for Debian and Rpm [\#256](https://github.com/sbt/sbt-native-packager/pull/256) ([kardapoltsev](https://github.com/kardapoltsev))
- support RPM 'Prefix'.   [\#242](https://github.com/sbt/sbt-native-packager/pull/242) ([jayaras](https://github.com/jayaras))

## [v0.7.2-RC1](https://github.com/sbt/sbt-native-packager/tree/v0.7.2-RC1) (2014-05-22)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.7.1...v0.7.2-RC1)

**Fixed bugs:**

- Fixing wrong usage of brackets in upstart script [\#262](https://github.com/sbt/sbt-native-packager/pull/262) ([muuki88](https://github.com/muuki88))

**Closed issues:**

- 0.7.0 - printlns upon entering sbt [\#263](https://github.com/sbt/sbt-native-packager/issues/263)

**Merged pull requests:**

- Setting correct version and adding import statements [\#261](https://github.com/sbt/sbt-native-packager/pull/261) ([muuki88](https://github.com/muuki88))

## [v0.7.1](https://github.com/sbt/sbt-native-packager/tree/v0.7.1) (2014-05-19)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.7.0...v0.7.1)

**Fixed bugs:**

- bash script too verbose pr default [\#252](https://github.com/sbt/sbt-native-packager/issues/252)
- RPM build fails on Kubuntu 11.04 [\#4](https://github.com/sbt/sbt-native-packager/issues/4)
- brpJavaRepackJar template fixed [\#254](https://github.com/sbt/sbt-native-packager/pull/254) ([kardapoltsev](https://github.com/kardapoltsev))

**Closed issues:**

- JavaServerApplication startup order [\#239](https://github.com/sbt/sbt-native-packager/issues/239)
- In windows java parmeters are not received [\#155](https://github.com/sbt/sbt-native-packager/issues/155)

**Merged pull requests:**

- Fix non Java 8 MaxPermSize configuration [\#258](https://github.com/sbt/sbt-native-packager/pull/258) ([jroper](https://github.com/jroper))
- FIX \#252 Removing the declare=0 variable [\#255](https://github.com/sbt/sbt-native-packager/pull/255) ([muuki88](https://github.com/muuki88))
- Do not try to setup missing deployment settings for Docker. [\#251](https://github.com/sbt/sbt-native-packager/pull/251) ([fiadliel](https://github.com/fiadliel))
- FIX \#239. Start run levels, dependencies in upstart and systemV impl [\#248](https://github.com/sbt/sbt-native-packager/pull/248) ([kardapoltsev](https://github.com/kardapoltsev))
- WIP: Provide task to stage output in a format sufficient to build Docker images [\#236](https://github.com/sbt/sbt-native-packager/pull/236) ([fiadliel](https://github.com/fiadliel))
- Make .bat start script honor -J and -D arguments. [\#218](https://github.com/sbt/sbt-native-packager/pull/218) ([gourlaysama](https://github.com/gourlaysama))

## [v0.7.0](https://github.com/sbt/sbt-native-packager/tree/v0.7.0) (2014-05-12)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.7.0-RC3...v0.7.0)

**Fixed bugs:**

- rpm:packageBin broken in 0.7.0-RC3 [\#240](https://github.com/sbt/sbt-native-packager/issues/240)
- Duplicated -X entries with etc-default-template [\#211](https://github.com/sbt/sbt-native-packager/issues/211)

**Closed issues:**

- No option to have JavaServer run as a user with a login shell [\#235](https://github.com/sbt/sbt-native-packager/issues/235)
- RPM error "Unable to open temp file" [\#228](https://github.com/sbt/sbt-native-packager/issues/228)
- multi module project jar is included twice [\#227](https://github.com/sbt/sbt-native-packager/issues/227)

**Merged pull requests:**

- FIX \#211 and \#232. Checking $java\_args\[@\] for memory settings, too. [\#246](https://github.com/sbt/sbt-native-packager/pull/246) ([muuki88](https://github.com/muuki88))
- ability to specify java server app start up order in debian implemented [\#245](https://github.com/sbt/sbt-native-packager/pull/245) ([kardapoltsev](https://github.com/kardapoltsev))
- Fix typos [\#244](https://github.com/sbt/sbt-native-packager/pull/244) ([Blaisorblade](https://github.com/Blaisorblade))
- New setting daemonShell.  [\#237](https://github.com/sbt/sbt-native-packager/pull/237) ([muuki88](https://github.com/muuki88))
- Adding documentation in universal getting started guide. [\#233](https://github.com/sbt/sbt-native-packager/pull/233) ([muuki88](https://github.com/muuki88))
- Add Debian script header [\#231](https://github.com/sbt/sbt-native-packager/pull/231) ([dax](https://github.com/dax))
- Fix for RPM error "Unable to open temp file" \#228 [\#229](https://github.com/sbt/sbt-native-packager/pull/229) ([grahamar](https://github.com/grahamar))

## [v0.7.0-RC3](https://github.com/sbt/sbt-native-packager/tree/v0.7.0-RC3) (2014-04-11)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.7.0-RC2...v0.7.0-RC3)

**Fixed bugs:**

- 0.7.0-RC2 uses brp-strip-static-archive but that script has been removed in OpenSuse [\#215](https://github.com/sbt/sbt-native-packager/issues/215)
- FIX \#214 Making rpm scriptlets overrideable [\#219](https://github.com/sbt/sbt-native-packager/pull/219) ([muuki88](https://github.com/muuki88))

**Closed issues:**

- debian postinst chowns install path [\#223](https://github.com/sbt/sbt-native-packager/issues/223)
- Creation of the daemon user and group is missing when installing a generated rpm on CentOS 6 \( in v. 0.7.0-RC2 \) [\#221](https://github.com/sbt/sbt-native-packager/issues/221)
- clean takes 12sec [\#220](https://github.com/sbt/sbt-native-packager/issues/220)
- RPM doesn't enable SystemV services [\#214](https://github.com/sbt/sbt-native-packager/issues/214)
- Need RUN\_OPTS in systemv bash archetype [\#196](https://github.com/sbt/sbt-native-packager/issues/196)
- Vagrant VMs [\#163](https://github.com/sbt/sbt-native-packager/issues/163)
- Creating a "Tested On" Wiki/Doc page [\#114](https://github.com/sbt/sbt-native-packager/issues/114)

**Merged pull requests:**

- Adding better MappingsHelper docs [\#226](https://github.com/sbt/sbt-native-packager/pull/226) ([muuki88](https://github.com/muuki88))
- Wrong permissions for /usr/share/app\_name fixed [\#225](https://github.com/sbt/sbt-native-packager/pull/225) ([kardapoltsev](https://github.com/kardapoltsev))
- Adding documentation on the repackaging option [\#216](https://github.com/sbt/sbt-native-packager/pull/216) ([muuki88](https://github.com/muuki88))
- Doc/version fixes for 0.7.0 release [\#213](https://github.com/sbt/sbt-native-packager/pull/213) ([jsuereth](https://github.com/jsuereth))

## [v0.7.0-RC2](https://github.com/sbt/sbt-native-packager/tree/v0.7.0-RC2) (2014-04-03)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.7.0-RC1...v0.7.0-RC2)

**Fixed bugs:**

- deb postinst chmod more directories [\#210](https://github.com/sbt/sbt-native-packager/issues/210)
- Java property values with spaces do not appear to be handled correctly [\#205](https://github.com/sbt/sbt-native-packager/issues/205)
- Does not package subprojects with sbt 0.13.2-M3 [\#197](https://github.com/sbt/sbt-native-packager/issues/197)
- Staging does not clear out previous files [\#175](https://github.com/sbt/sbt-native-packager/issues/175)
- Correct scoping for daemonGroup [\#194](https://github.com/sbt/sbt-native-packager/pull/194) ([muuki88](https://github.com/muuki88))

**Closed issues:**

- Memory settings for Java 8 [\#209](https://github.com/sbt/sbt-native-packager/issues/209)
- Don't pass MaxPermSize to Java 8+ [\#203](https://github.com/sbt/sbt-native-packager/issues/203)
- RPM creation is hella slow? [\#195](https://github.com/sbt/sbt-native-packager/issues/195)
- Must set 'daemonUser in Linux' if daemonUser != name [\#193](https://github.com/sbt/sbt-native-packager/issues/193)
- Release an RC1 of 0.7.0 [\#191](https://github.com/sbt/sbt-native-packager/issues/191)
- sbt-git and sbt-native-packager [\#125](https://github.com/sbt/sbt-native-packager/issues/125)

**Merged pull requests:**

- Wip/getting started guide additions [\#212](https://github.com/sbt/sbt-native-packager/pull/212) ([muuki88](https://github.com/muuki88))
- First cut at a getting started guide which is more useful than what we had [\#208](https://github.com/sbt/sbt-native-packager/pull/208) ([jsuereth](https://github.com/jsuereth))
- Handle spaces in java arguments properly [\#207](https://github.com/sbt/sbt-native-packager/pull/207) ([jsuereth](https://github.com/jsuereth))
- small clean up in bash template [\#206](https://github.com/sbt/sbt-native-packager/pull/206) ([kardapoltsev](https://github.com/kardapoltsev))
- FIX \#203 Adding check for java 8 [\#204](https://github.com/sbt/sbt-native-packager/pull/204) ([muuki88](https://github.com/muuki88))
- Fixes \#175 - Staging does not clear previous files. [\#202](https://github.com/sbt/sbt-native-packager/pull/202) ([jsuereth](https://github.com/jsuereth))
- testing for /var/run/app\_name directory instead of file existence in debian and rpm templates [\#201](https://github.com/sbt/sbt-native-packager/pull/201) ([kardapoltsev](https://github.com/kardapoltsev))
- Ensure /var/run/app\_name/ exists when starting [\#200](https://github.com/sbt/sbt-native-packager/pull/200) ([nemunaire](https://github.com/nemunaire))
- Fix \#195 Adding rpmBrpJavaRepackJars setting  [\#199](https://github.com/sbt/sbt-native-packager/pull/199) ([muuki88](https://github.com/muuki88))

## [v0.7.0-RC1](https://github.com/sbt/sbt-native-packager/tree/v0.7.0-RC1) (2014-03-12)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.7.0-M3...v0.7.0-RC1)

**Fixed bugs:**

- Allow to override upstart script templates [\#182](https://github.com/sbt/sbt-native-packager/issues/182)
- pid file creation as non-root [\#164](https://github.com/sbt/sbt-native-packager/issues/164)
- Specifying a post rpm command breaks java\_server [\#76](https://github.com/sbt/sbt-native-packager/issues/76)

**Closed issues:**

- broken link in readme [\#173](https://github.com/sbt/sbt-native-packager/issues/173)
- Add a human readable helper method for directory mappings [\#161](https://github.com/sbt/sbt-native-packager/issues/161)
- Ability to add a directories and their content recursively to java application [\#158](https://github.com/sbt/sbt-native-packager/issues/158)
- rpm java\_server archetype - systemV init scripts [\#149](https://github.com/sbt/sbt-native-packager/issues/149)
- How to get well generated paths in a start script of a Debian package? [\#115](https://github.com/sbt/sbt-native-packager/issues/115)
- Support for BuildArch SPEC configuration [\#112](https://github.com/sbt/sbt-native-packager/issues/112)
- rpm and directories [\#92](https://github.com/sbt/sbt-native-packager/issues/92)
- Documentation: src/sphinx/gettingstarted.rst has deb and rpm switched [\#91](https://github.com/sbt/sbt-native-packager/issues/91)
- Need to be able to specify what user and group the RPM installs files [\#75](https://github.com/sbt/sbt-native-packager/issues/75)
- Java Server Application: Add support for CentOS [\#70](https://github.com/sbt/sbt-native-packager/issues/70)

**Merged pull requests:**

- Unify linux script replacements \#182 [\#192](https://github.com/sbt/sbt-native-packager/pull/192) ([muuki88](https://github.com/muuki88))
- Adding some test for overriding debian templates [\#190](https://github.com/sbt/sbt-native-packager/pull/190) ([muuki88](https://github.com/muuki88))
- Removing `normalizedKey` in packagerSettings. \#188 [\#189](https://github.com/sbt/sbt-native-packager/pull/189) ([muuki88](https://github.com/muuki88))
- Adding additional features for rpm [\#187](https://github.com/sbt/sbt-native-packager/pull/187) ([muuki88](https://github.com/muuki88))
- change parameter ordering for rpmbuild, fixes ignoring arch on OSX [\#186](https://github.com/sbt/sbt-native-packager/pull/186) ([cchampignon](https://github.com/cchampignon))
- Support `--' to stop parsing options of sbt-native-packager itself. [\#184](https://github.com/sbt/sbt-native-packager/pull/184) ([tksk](https://github.com/tksk))
- Apply scalariform test first time [\#183](https://github.com/sbt/sbt-native-packager/pull/183) ([muuki88](https://github.com/muuki88))
- pid file location changed for play and systemV start template [\#180](https://github.com/sbt/sbt-native-packager/pull/180) ([kardapoltsev](https://github.com/kardapoltsev))
- Adding scalariform with default settings [\#179](https://github.com/sbt/sbt-native-packager/pull/179) ([muuki88](https://github.com/muuki88))
- Implementing permissions as described in \#174 [\#178](https://github.com/sbt/sbt-native-packager/pull/178) ([muuki88](https://github.com/muuki88))
- Fixing scriptlets and adding tests [\#177](https://github.com/sbt/sbt-native-packager/pull/177) ([muuki88](https://github.com/muuki88))
- Wip/rpm server archetype [\#176](https://github.com/sbt/sbt-native-packager/pull/176) ([muuki88](https://github.com/muuki88))
- Wip/server permissions [\#174](https://github.com/sbt/sbt-native-packager/pull/174) ([muuki88](https://github.com/muuki88))
- Wip/package mapping tests [\#172](https://github.com/sbt/sbt-native-packager/pull/172) ([muuki88](https://github.com/muuki88))
- start template clean up to be used with new /etc/default [\#171](https://github.com/sbt/sbt-native-packager/pull/171) ([kardapoltsev](https://github.com/kardapoltsev))
- Fix Windows msi installer PATH entry [\#169](https://github.com/sbt/sbt-native-packager/pull/169) ([rvs1257](https://github.com/rvs1257))
- Make start script more portable by using /usr/bin/env as shebang. [\#167](https://github.com/sbt/sbt-native-packager/pull/167) ([alexdupre](https://github.com/alexdupre))
- mistype fixed in rpm keys [\#166](https://github.com/sbt/sbt-native-packager/pull/166) ([kardapoltsev](https://github.com/kardapoltsev))
- Attempt to resolve issue 161 - Add human readable methods to map directories [\#165](https://github.com/sbt/sbt-native-packager/pull/165) ([ivanfrain](https://github.com/ivanfrain))
- Adding documentation how to map directories [\#160](https://github.com/sbt/sbt-native-packager/pull/160) ([muuki88](https://github.com/muuki88))
- Adding automation for releasing \*and\* directly release to bintray. [\#157](https://github.com/sbt/sbt-native-packager/pull/157) ([jsuereth](https://github.com/jsuereth))
- FIX \#112 Adding BuildArch to the spec file [\#156](https://github.com/sbt/sbt-native-packager/pull/156) ([muuki88](https://github.com/muuki88))
- Adding tests for \#76 [\#154](https://github.com/sbt/sbt-native-packager/pull/154) ([muuki88](https://github.com/muuki88))

## [v0.7.0-M3](https://github.com/sbt/sbt-native-packager/tree/v0.7.0-M3) (2014-02-04)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.7.0-M2...v0.7.0-M3)

**Fixed bugs:**

- 0.7.0-M2  -  sym link in /usr/bin is wrong - points to wrong location [\#151](https://github.com/sbt/sbt-native-packager/issues/151)

**Closed issues:**

- Debian - Warnings on install [\#136](https://github.com/sbt/sbt-native-packager/issues/136)

**Merged pull requests:**

- Wrong symlink location [\#153](https://github.com/sbt/sbt-native-packager/pull/153) ([muuki88](https://github.com/muuki88))
- Use defaultLinuxLogsLocation for /var/log [\#150](https://github.com/sbt/sbt-native-packager/pull/150) ([hfs](https://github.com/hfs))
- Making SystemV start script LSB and Debian compliant [\#148](https://github.com/sbt/sbt-native-packager/pull/148) ([aparkinson](https://github.com/aparkinson))

## [v0.7.0-M2](https://github.com/sbt/sbt-native-packager/tree/v0.7.0-M2) (2014-02-01)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.7.0-M1...v0.7.0-M2)

**Closed issues:**

- The specified `daemonUser` is not the owner of the installation directory [\#129](https://github.com/sbt/sbt-native-packager/issues/129)
- After dist, play do not pick up jars from lib directory [\#123](https://github.com/sbt/sbt-native-packager/issues/123)
- exec $java\_cmd vs simple $java\_cmd in bash script [\#99](https://github.com/sbt/sbt-native-packager/issues/99)
- How to specify JAVA\_OPTS for JavaServer archetype? [\#98](https://github.com/sbt/sbt-native-packager/issues/98)
- bash start script fails with java\_cmd: readonly variable [\#94](https://github.com/sbt/sbt-native-packager/issues/94)
- Add unit to comment describing memory integer [\#90](https://github.com/sbt/sbt-native-packager/issues/90)
- Unable to generate deb file in play 2.2-RC2 [\#37](https://github.com/sbt/sbt-native-packager/issues/37)
- Possible race condition in zip creation? [\#23](https://github.com/sbt/sbt-native-packager/issues/23)

**Merged pull requests:**

- Fix \#37 Adding meaningful logging, when packageDescription in Debian is [\#146](https://github.com/sbt/sbt-native-packager/pull/146) ([muuki88](https://github.com/muuki88))
- FIX \#90 Adding memory unit for `-mem` option in bash script [\#145](https://github.com/sbt/sbt-native-packager/pull/145) ([muuki88](https://github.com/muuki88))
- Use normalizedName as the default appUser for Linux Packages [\#144](https://github.com/sbt/sbt-native-packager/pull/144) ([aparkinson](https://github.com/aparkinson))
- appUser and appGroup now correctly own the installation directory [\#143](https://github.com/sbt/sbt-native-packager/pull/143) ([aparkinson](https://github.com/aparkinson))
- Typo in test name: mutliproject -\> multiproject [\#141](https://github.com/sbt/sbt-native-packager/pull/141) ([hfs](https://github.com/hfs))
- Fix \#98 java server type includes /etc/default by default [\#140](https://github.com/sbt/sbt-native-packager/pull/140) ([muuki88](https://github.com/muuki88))
- Change the default owner of packaged files. See \#129 [\#139](https://github.com/sbt/sbt-native-packager/pull/139) ([aparkinson](https://github.com/aparkinson))
- unify upstart and  system v start scripts [\#138](https://github.com/sbt/sbt-native-packager/pull/138) ([jsuereth](https://github.com/jsuereth))
- systemV start script refactored with new bash script [\#137](https://github.com/sbt/sbt-native-packager/pull/137) ([kardapoltsev](https://github.com/kardapoltsev))
- Adding Travis-CI configuration for automated pull requests. [\#133](https://github.com/sbt/sbt-native-packager/pull/133) ([jsuereth](https://github.com/jsuereth))
- Adding tests for default upstart configuration [\#132](https://github.com/sbt/sbt-native-packager/pull/132) ([muuki88](https://github.com/muuki88))
- Started refactoring of archetypes templates [\#131](https://github.com/sbt/sbt-native-packager/pull/131) ([muuki88](https://github.com/muuki88))
- Bring the SystemV init.d scripts inline with the Debian policies [\#130](https://github.com/sbt/sbt-native-packager/pull/130) ([aparkinson](https://github.com/aparkinson))
- Checking if a user already exists during postinstall is missing the user name to check for [\#128](https://github.com/sbt/sbt-native-packager/pull/128) ([aparkinson](https://github.com/aparkinson))
- Adding some documentation to the debian package. [\#127](https://github.com/sbt/sbt-native-packager/pull/127) ([muuki88](https://github.com/muuki88))
- Debian Control File - Invalid package name [\#126](https://github.com/sbt/sbt-native-packager/pull/126) ([muuki88](https://github.com/muuki88))
- Cleaning up Lintian errors and warnings [\#122](https://github.com/sbt/sbt-native-packager/pull/122) ([aparkinson](https://github.com/aparkinson))
- Better upstart script [\#120](https://github.com/sbt/sbt-native-packager/pull/120) ([dbathily](https://github.com/dbathily))

## [v0.7.0-M1](https://github.com/sbt/sbt-native-packager/tree/v0.7.0-M1) (2014-01-06)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.6.3...v0.7.0-M1)

**Closed issues:**

- how to get the latest snapshot code [\#119](https://github.com/sbt/sbt-native-packager/issues/119)
- Allow to override templates [\#118](https://github.com/sbt/sbt-native-packager/issues/118)
- Dependency with classifier not included [\#102](https://github.com/sbt/sbt-native-packager/issues/102)
- Bad DebianMaintainerScripts permissions [\#96](https://github.com/sbt/sbt-native-packager/issues/96)
- Release 0.6.4 [\#77](https://github.com/sbt/sbt-native-packager/issues/77)
- Specify location where RPM installs files [\#74](https://github.com/sbt/sbt-native-packager/issues/74)
- java.lang.NoClassDefFoundError [\#73](https://github.com/sbt/sbt-native-packager/issues/73)
- failed to create symbolic link [\#71](https://github.com/sbt/sbt-native-packager/issues/71)
- folder mappings [\#69](https://github.com/sbt/sbt-native-packager/issues/69)
- Clarify about "Java Server Application" in README [\#68](https://github.com/sbt/sbt-native-packager/issues/68)
- Add custom directory to app\_classpath [\#66](https://github.com/sbt/sbt-native-packager/issues/66)
- File Permissions - user isn't set? [\#62](https://github.com/sbt/sbt-native-packager/issues/62)
- Template Directories for Debian package [\#54](https://github.com/sbt/sbt-native-packager/issues/54)
- Directories in packageMapping [\#6](https://github.com/sbt/sbt-native-packager/issues/6)

**Merged pull requests:**

- Allow script templates to be overridden [\#121](https://github.com/sbt/sbt-native-packager/pull/121) ([jsuereth](https://github.com/jsuereth))
- debian:package-bin throws a FileNotFoundException for missing postrm [\#117](https://github.com/sbt/sbt-native-packager/pull/117) ([aparkinson](https://github.com/aparkinson))
- Allow the upstart job to be executed as a different user [\#116](https://github.com/sbt/sbt-native-packager/pull/116) ([aparkinson](https://github.com/aparkinson))
- Users.Root constant added. [\#113](https://github.com/sbt/sbt-native-packager/pull/113) ([kardapoltsev](https://github.com/kardapoltsev))
- /var/log/app\_name permissions fixed [\#111](https://github.com/sbt/sbt-native-packager/pull/111) ([kardapoltsev](https://github.com/kardapoltsev))
- fixed bad permissions for debian maintainer scripts [\#110](https://github.com/sbt/sbt-native-packager/pull/110) ([kardapoltsev](https://github.com/kardapoltsev))
- Added status command to sysvinit template [\#108](https://github.com/sbt/sbt-native-packager/pull/108) ([kardapoltsev](https://github.com/kardapoltsev))
- Directories in packageMapping \#6 [\#107](https://github.com/sbt/sbt-native-packager/pull/107) ([muuki88](https://github.com/muuki88))
- Default values for packageArchetype.java\_server [\#106](https://github.com/sbt/sbt-native-packager/pull/106) ([muuki88](https://github.com/muuki88))
- Setting chdir to application directory.  [\#105](https://github.com/sbt/sbt-native-packager/pull/105) ([muuki88](https://github.com/muuki88))
- Fixed documentation for commands that were changed from camel case to dash-separated [\#101](https://github.com/sbt/sbt-native-packager/pull/101) ([jfim](https://github.com/jfim))
- adds option to disable the java\_version\_check on linux [\#100](https://github.com/sbt/sbt-native-packager/pull/100) ([vchuravy](https://github.com/vchuravy))
- Fixed: user defined scripts in debian overridden by generated files [\#97](https://github.com/sbt/sbt-native-packager/pull/97) ([kardapoltsev](https://github.com/kardapoltsev))
- Do not declare java\_cmd read-only as process\_args will override it [\#95](https://github.com/sbt/sbt-native-packager/pull/95) ([hackmann](https://github.com/hackmann))
- generating etc default, different postinst script for sysvinit [\#93](https://github.com/sbt/sbt-native-packager/pull/93) ([kardapoltsev](https://github.com/kardapoltsev))
- Restart option added to sysvinit script [\#89](https://github.com/sbt/sbt-native-packager/pull/89) ([kardapoltsev](https://github.com/kardapoltsev))
- error creating symlink when file already exists fixed [\#86](https://github.com/sbt/sbt-native-packager/pull/86) ([kardapoltsev](https://github.com/kardapoltsev))
- Sysvinit script for debian package [\#85](https://github.com/sbt/sbt-native-packager/pull/85) ([kardapoltsev](https://github.com/kardapoltsev))
- Implemented chown file permissions and user/group creation [\#84](https://github.com/sbt/sbt-native-packager/pull/84) ([muuki88](https://github.com/muuki88))
- Informative failure for missing files mapped in universal [\#83](https://github.com/sbt/sbt-native-packager/pull/83) ([gmjabs](https://github.com/gmjabs))
- Fix wrong String.replaceAll in JavaAppBatScript.makeWindowsRelativeClasspathDefine [\#81](https://github.com/sbt/sbt-native-packager/pull/81) ([chwthewke](https://github.com/chwthewke))
- Fixes and improvements for usability. [\#78](https://github.com/sbt/sbt-native-packager/pull/78) ([jsuereth](https://github.com/jsuereth))
- Fix duplicate jar name when artifact classifier is used [\#67](https://github.com/sbt/sbt-native-packager/pull/67) ([bbarkley](https://github.com/bbarkley))
- Removing JavaC dependency in \*.bat [\#65](https://github.com/sbt/sbt-native-packager/pull/65) ([Jentsch](https://github.com/Jentsch))
- Precise location of Universal configuration [\#64](https://github.com/sbt/sbt-native-packager/pull/64) ([yanns](https://github.com/yanns))
- task \#54: Template Directories for Debian package [\#63](https://github.com/sbt/sbt-native-packager/pull/63) ([muuki88](https://github.com/muuki88))
- Fixing debian package installation/deinstallation [\#60](https://github.com/sbt/sbt-native-packager/pull/60) ([muuki88](https://github.com/muuki88))
- bat-template pass arguments [\#59](https://github.com/sbt/sbt-native-packager/pull/59) ([rmgk](https://github.com/rmgk))

## [v0.6.3](https://github.com/sbt/sbt-native-packager/tree/v0.6.3) (2013-11-01)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.6.2...v0.6.3)

**Closed issues:**

- ZIP file names do not extract correctly if ZIP is created on Windows and unzipped on Linux [\#55](https://github.com/sbt/sbt-native-packager/issues/55)
- get\_mem\_opts interferes with java\_opts [\#48](https://github.com/sbt/sbt-native-packager/issues/48)
- JAVA\_OPTS ignored [\#47](https://github.com/sbt/sbt-native-packager/issues/47)
- Uninformative exception when `head` called on empty list [\#46](https://github.com/sbt/sbt-native-packager/issues/46)
- Windows MSI + Play 2.2.0 : `CNDL0027 : The Shortcut/@Name attribute's value, '\application.conf', is not a valid long name because it contains illegal characters` [\#43](https://github.com/sbt/sbt-native-packager/issues/43)
- Upstart Script for Linux Distros [\#42](https://github.com/sbt/sbt-native-packager/issues/42)
- BashTemplate - problem resolving relative symlinks on the bash file \(not the directory\) [\#39](https://github.com/sbt/sbt-native-packager/issues/39)
- Linux unfriendly default naming [\#38](https://github.com/sbt/sbt-native-packager/issues/38)
- Debian postinst/preinst [\#35](https://github.com/sbt/sbt-native-packager/issues/35)
- Change sourceDirectory and/or stageDirectory in Universal from Settings to Tasks [\#34](https://github.com/sbt/sbt-native-packager/issues/34)

**Merged pull requests:**

- Adding some assertions and corrected description in Keys [\#58](https://github.com/sbt/sbt-native-packager/pull/58) ([muuki88](https://github.com/muuki88))
- Debian scripts are now picked up from default folder \#35 [\#57](https://github.com/sbt/sbt-native-packager/pull/57) ([muuki88](https://github.com/muuki88))
- Issue \#55 - Correct ZIP entry names when created on a Windows system [\#56](https://github.com/sbt/sbt-native-packager/pull/56) ([doswell](https://github.com/doswell))
- First commit on \#35 [\#53](https://github.com/sbt/sbt-native-packager/pull/53) ([muuki88](https://github.com/muuki88))
- Fixes \#43 - unable to create MSI of play application [\#50](https://github.com/sbt/sbt-native-packager/pull/50) ([jsuereth](https://github.com/jsuereth))
- Update README.md [\#49](https://github.com/sbt/sbt-native-packager/pull/49) ([schmmd](https://github.com/schmmd))
- Fixes \#39 - Unable to follow symlinks. [\#45](https://github.com/sbt/sbt-native-packager/pull/45) ([jsuereth](https://github.com/jsuereth))
- Working on \#42 [\#44](https://github.com/sbt/sbt-native-packager/pull/44) ([muuki88](https://github.com/muuki88))
- Prefer normalizedName for linux-friendly packages, rather than requiring... [\#41](https://github.com/sbt/sbt-native-packager/pull/41) ([jsuereth](https://github.com/jsuereth))
- Fix \#11 debian symlinks now absolute. Maybe by default for all? [\#40](https://github.com/sbt/sbt-native-packager/pull/40) ([muuki88](https://github.com/muuki88))
- fix two typos in code examples in the doc. [\#36](https://github.com/sbt/sbt-native-packager/pull/36) ([gourlaysama](https://github.com/gourlaysama))
- Fixed typo, shortened hash string padding and removed debug printlns [\#33](https://github.com/sbt/sbt-native-packager/pull/33) ([ptrbrtz](https://github.com/ptrbrtz))

## [v0.6.2](https://github.com/sbt/sbt-native-packager/tree/v0.6.2) (2013-09-03)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.6.1...v0.6.2)

**Closed issues:**

- Duplicate entries in zip [\#25](https://github.com/sbt/sbt-native-packager/issues/25)
- Readme / Usage typo [\#22](https://github.com/sbt/sbt-native-packager/issues/22)

**Merged pull requests:**

- Fixed two bugs [\#32](https://github.com/sbt/sbt-native-packager/pull/32) ([ptrbrtz](https://github.com/ptrbrtz))
- Everyone needs a little gitignore [\#31](https://github.com/sbt/sbt-native-packager/pull/31) ([jroper](https://github.com/jroper))
- Windows customisation [\#30](https://github.com/sbt/sbt-native-packager/pull/30) ([jroper](https://github.com/jroper))
- Append newline after template\_declares substitution [\#29](https://github.com/sbt/sbt-native-packager/pull/29) ([jroper](https://github.com/jroper))
- More relative path checks for the bash script's realpath processing [\#28](https://github.com/sbt/sbt-native-packager/pull/28) ([huntc](https://github.com/huntc))
- Fix addApp function to append to app\_commands [\#27](https://github.com/sbt/sbt-native-packager/pull/27) ([jroper](https://github.com/jroper))
- Ensures distinct dependencies are only added [\#26](https://github.com/sbt/sbt-native-packager/pull/26) ([huntc](https://github.com/huntc))
- Exclude non-required artifacts from the lib folder [\#24](https://github.com/sbt/sbt-native-packager/pull/24) ([huntc](https://github.com/huntc))

## [v0.6.1](https://github.com/sbt/sbt-native-packager/tree/v0.6.1) (2013-08-21)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.6.0...v0.6.1)

**Closed issues:**

- include subprojects in parent aggregator [\#19](https://github.com/sbt/sbt-native-packager/issues/19)

**Merged pull requests:**

- Fixes issue where classpath of inter-project-dependencies didn't make a distirbution [\#20](https://github.com/sbt/sbt-native-packager/pull/20) ([jsuereth](https://github.com/jsuereth))

## [v0.6.0](https://github.com/sbt/sbt-native-packager/tree/v0.6.0) (2013-07-12)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/0.5.4...v0.6.0)

**Merged pull requests:**

- Wip/cleanup for play [\#18](https://github.com/sbt/sbt-native-packager/pull/18) ([jsuereth](https://github.com/jsuereth))
- Wip/stage and dist [\#17](https://github.com/sbt/sbt-native-packager/pull/17) ([jsuereth](https://github.com/jsuereth))
- Fix formatting in README [\#15](https://github.com/sbt/sbt-native-packager/pull/15) ([calvinkrishy](https://github.com/calvinkrishy))
- Add a layer between universal + specific native packages [\#14](https://github.com/sbt/sbt-native-packager/pull/14) ([jsuereth](https://github.com/jsuereth))
- Added bare-minimum support for creating apple's DMG files. [\#13](https://github.com/sbt/sbt-native-packager/pull/13) ([jsuereth](https://github.com/jsuereth))
- RPM Spec Automatic Dependencies [\#11](https://github.com/sbt/sbt-native-packager/pull/11) ([pussinboots](https://github.com/pussinboots))

## [0.5.4](https://github.com/sbt/sbt-native-packager/tree/0.5.4) (2013-03-07)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.5.2...0.5.4)

**Merged pull requests:**

- Add support for package maintainer scripts. [\#10](https://github.com/sbt/sbt-native-packager/pull/10) ([saeta](https://github.com/saeta))

## [v0.5.2](https://github.com/sbt/sbt-native-packager/tree/v0.5.2) (2013-02-19)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.5.1...v0.5.2)

## [v0.5.1](https://github.com/sbt/sbt-native-packager/tree/v0.5.1) (2013-02-19)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/v0.5.0...v0.5.1)

**Closed issues:**

- Confilict with sbtscalariform [\#8](https://github.com/sbt/sbt-native-packager/issues/8)

## [v0.5.0](https://github.com/sbt/sbt-native-packager/tree/v0.5.0) (2013-01-04)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/0.4.4...v0.5.0)

**Merged pull requests:**

- Adding support for rpm scriptlets [\#9](https://github.com/sbt/sbt-native-packager/pull/9) ([fleipold](https://github.com/fleipold))

## [0.4.4](https://github.com/sbt/sbt-native-packager/tree/0.4.4) (2012-08-01)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/0.4.3...0.4.4)

**Merged pull requests:**

- Typos in documentation.  [\#7](https://github.com/sbt/sbt-native-packager/pull/7) ([iammichiel](https://github.com/iammichiel))
- RPM doesn't accept filenames with whitespace [\#5](https://github.com/sbt/sbt-native-packager/pull/5) ([Dremora](https://github.com/Dremora))

## [0.4.3](https://github.com/sbt/sbt-native-packager/tree/0.4.3) (2012-06-28)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/0.4.2...0.4.3)

## [0.4.2](https://github.com/sbt/sbt-native-packager/tree/0.4.2) (2012-05-09)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/0.4.1...0.4.2)

## [0.4.1](https://github.com/sbt/sbt-native-packager/tree/0.4.1) (2012-04-14)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/0.4...0.4.1)

## [0.4](https://github.com/sbt/sbt-native-packager/tree/0.4) (2012-03-28)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/0.3.0...0.4)

## [0.3.0](https://github.com/sbt/sbt-native-packager/tree/0.3.0) (2012-03-15)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/0.1.0...0.3.0)

**Closed issues:**

- Packaging fails for RPM & DEB if there is a space in the directory path [\#3](https://github.com/sbt/sbt-native-packager/issues/3)

**Merged pull requests:**

- Fix rpmbuild syntax in RpmHelper [\#2](https://github.com/sbt/sbt-native-packager/pull/2) ([mtye](https://github.com/mtye))
- Fix plugin organization in \_Getting Started\_ documentation. [\#1](https://github.com/sbt/sbt-native-packager/pull/1) ([mtye](https://github.com/mtye))

## [0.1.0](https://github.com/sbt/sbt-native-packager/tree/0.1.0) (2012-01-16)

[Full Changelog](https://github.com/sbt/sbt-native-packager/compare/9ae9a8ac8125b3af7d9c14864bbcf26d1597203b...0.1.0)



\* *This Changelog was automatically generated by [github_changelog_generator](https://github.com/github-changelog-generator/github-changelog-generator)*
