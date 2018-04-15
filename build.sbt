import sbt.Resolver

name := "site_parser"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.5"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

resolvers += Resolver.jcenterRepo

libraryDependencies ++= Seq(
  ehcache,
  filters,
  guice,
  ws,
  "org.webjars"       %   "swagger-ui"                        % "3.13.3",
  "com.iheart"        %%  "play-swagger"                      % "0.7.4",
  "org.webjars"       %%  "webjars-play"                      % "2.6.3",
  "net.ruippeixotog"  %%  "scala-scraper"                     % "2.1.0"
)