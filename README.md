Install:
- move to project folder and type ./gradlew installDir in console
- move into build\install\examples\bin and start riddle-server.bat

Riddle Client:
a client that can generate Riddles and solve Riddles via text input.

 CommandList:
  - !help
  - !requestRiddle
  - !showRiddle
  - !solveRiddle
  - !setOutput x
  - !stopSolve
  - !bruteRiddle

Camel Client:
connects to a mqtt server to get Riddles (broker Url has to be set in CamelClient.java!!!)
solve the riddle and sends it back to mqtt via camel
