from PIL import Image
import os

weapon_files = [
    "assets/pistol.png",
    "assets/rifle.png",
    "assets/sniper.png",
    "assets/netgun.png",
    "assets/sleepygun.png",
    "assets/dartgun.png",
    "assets/grenede.png",
    "assets/freezegun.png",
]

def is_background(r, g, b):
    # 白底、淺灰底、棋盤格常見灰白色都去掉
    if r > 225 and g > 225 and b > 225:
        return True

    # 常見透明棋盤格灰色
    if abs(r - g) < 8 and abs(g - b) < 8 and 170 <= r <= 230:
        return True

    return False

for path in weapon_files:
    if not os.path.exists(path):
        print(f"找不到：{path}")
        continue

    img = Image.open(path).convert("RGBA")
    pixels = img.load()

    width, height = img.size

    for y in range(height):
        for x in range(width):
            r, g, b, a = pixels[x, y]

            if is_background(r, g, b):
                pixels[x, y] = (r, g, b, 0)

    img.save(path)
    print(f"已去背：{path}")