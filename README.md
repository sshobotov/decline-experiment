Tickets4Sale
============

### Disclaimer

Despite a very simple idea of this project it might look a bit overcomplicated
but all functionality was built with a high testability and flexibility in mind.

Nevertheless code duplication is highly undesirable some of it is unavoidable
and was made on purpose: view models for `cli` and `api` modules seems like very
similar but even now has a slight difference, so to have a freedom to evolve
two modules independently (have a very different end users) views are kept
separately and somewhat duplicate each other.

### Prerequisites

- sbt

### Structure

- [core components](core/README.md)
- [CLI tool](cli/README.md)
- [Json API server](api/README.md)