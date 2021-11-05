let
  sources = import ./nix/sources.nix;
  overlays = import ./nix/overlays.nix;
  pkgs = import sources.nixpkgs {
    overlays = [ overlays ];
  };
in
pkgs.mkShell {
  buildInputs = [
    pkgs.jdk
    pkgs.sbt
    pkgs.coursier
    pkgs.metals
    pkgs.bloop
    pkgs.scalafmt
  ];
}
