{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-25.05";
    utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, utils }:
    utils.lib.eachDefaultSystem (system:
      let
        pkgs = nixpkgs.legacyPackages.${system};
      in
      {
        devShells.default = pkgs.mkShell {
          packages = with pkgs; [
            libcap
            go
            gcc
            garage_2_1_0
            bruno
          ];

          shellHook = ''
          '';
        };

        formatter = pkgs.nixpkgs-fmt;
      });
}
