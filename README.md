# RNSucks

Some plugins for Aliucord made for myself.

## Usage

1. Install [Aliucord](https://github.com/Aliucord/Aliucord)
2. Connect device with Aliucord installed to your computer (USB debugging required)
3. Run following commands in terminal:

On Linux / macOS:

```bash
$ git clone https://github.com/MCausc78/RNSucks
$ cd RNSucks
$ ./gradlew make
```

On Windows:

```bash
$ git clone https://github.com/MCausc78/RNSucks
$ cd RNSucks
$ .\gradlew.bat make
```

4. Copy `RNSucks/<PluginName>/build/<PluginName>.zip` to `/storage/emulated/0/Aliucord/plugins/`

During development you may use `./gradlew <PluginName>:deployWithAdb` instead.

## License

Everything in this repo is released into the public domain. You may use it however you want with no
conditions whatsoever
