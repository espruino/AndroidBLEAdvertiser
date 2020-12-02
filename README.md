Android BLE Advertiser
=======================


Every 15 minutes this app will fetch the webpage given and set your phone's Bluetooth advertising to Espruino's 0x0590 manufacturer data with the value from the RegEx's first match group.

By default it'll show you roughly [how many people have been rickrolled](https://www.youtube.com/watch?v=dQw4w9WgXcQ) to date.

Using an [Espruino Device](http://www.espruino.com/) you can then scan for the advertising data every so often:

```JS
setInterval(function () {
  NRF.requestDevice({ filters: [{ manufacturerData:{0x0590:{}} }] }).then(function(dev) {
    var info = E.toString(dev.manufacturerData);
    // by default, this would be the current view count...
    print(info);
  });
}, 10000); // every 10 seconds
```

**Note:** this app is unlikely to work if you have a Covid contact tracing app installed, as those apps will silently overwrite any Bluetooth advertising data with their own.

To get an APK to install, see the `builds` folder.
