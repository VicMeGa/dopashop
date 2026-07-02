# shell.nix
{ pkgs ? import <nixpkgs> {} }:

pkgs.mkShell {
  packages = [
    pkgs.jdk17
  ];

  shellHook = ''
    export JAVA_HOME=${pkgs.jdk17}/lib/openjdk
    echo "Java $(java -version 2>&1 | head -n1)"
  '';
}
