import math
from pathlib import Path

try:
    from PIL import Image
except ImportError as error:
    raise SystemExit(
        "缺少 Pillow 图片处理库，请先在当前 Python 环境执行：python -m pip install Pillow"
    ) from error

def main():
    base_dir = Path(__file__).resolve().parents[1]
    ui_dir = base_dir / "docs" / "ui" / "桌面和悬浮球图标"
    res_dir = base_dir / "app" / "src" / "main" / "res" / "drawable-nodpi"

    # 1. 确保输出目录存在
    res_dir.mkdir(parents=True, exist_ok=True)
    print(f"Output resource directory: {res_dir}")

    # 2. 生成桌面自适应前景图 (1080x1080，图标缩小到 70% 居中以满足 Safe Zone)
    app_icon_path = ui_dir / "app-icon-premium-20260528.png"
    if not app_icon_path.exists():
        print(f"ERROR: Cannot find app icon design: {app_icon_path}")
        return

    print("Processing app icon adaptive foreground...")
    img_icon = Image.open(app_icon_path).convert("RGBA")

    # 缩放到 1080 * 0.7 = 756 像素以容纳在 72dp 核心区和 66dp Safe Zone 圆形剪裁中
    icon_size = 756
    img_icon_resized = img_icon.resize((icon_size, icon_size), Image.Resampling.LANCZOS)

    # 贴在 1080x1080 完全透明 RGBA 画布上
    canvas = Image.new("RGBA", (1080, 1080), (0, 0, 0, 0))
    offset = (1080 - icon_size) // 2
    canvas.paste(img_icon_resized, (offset, offset), img_icon_resized)

    # 保存
    out_icon_path = res_dir / "ic_launcher_foreground_premium.png"
    canvas.save(out_icon_path, "PNG")
    print(f"SUCCESS: Generated {out_icon_path}")

    # 3. 处理悬浮球图像：裁切为圆形水晶球，圆心 (512, 512)，半径 420，并作 15 像素边缘羽化抗锯齿
    bubble_path = ui_dir / "floating-bubble-premium-20260528.png"
    if not bubble_path.exists():
        print(f"ERROR: Cannot find floating bubble design: {bubble_path}")
        return

    print("Processing floating bubble image (circular mask & anti-aliasing feathering)...")
    img_bubble = Image.open(bubble_path).convert("RGBA")
    width, height = img_bubble.size

    cx, cy = width / 2.0, height / 2.0
    r_in = 416.0
    r_out = 431.0  # 半径 416 以内保留 100% Alpha，416-431 羽化，大于 431 完全透明

    pixels = img_bubble.load()
    for y in range(height):
        for x in range(width):
            dx = x - cx + 0.5
            dy = y - cy + 0.5
            dist = math.sqrt(dx*dx + dy*dy)

            r, g, b, a = pixels[x, y]
            if dist <= r_in:
                pass
            elif dist >= r_out:
                pixels[x, y] = (r, g, b, 0)
            else:
                factor = (r_out - dist) / (r_out - r_in)
                new_a = int(a * factor)
                pixels[x, y] = (r, g, b, new_a)

    # 缩放到适合在 xxhdpi 屏幕上显示的 256x256 分辨率，在 58dp 下显示效果极其细腻
    img_bubble_resized = img_bubble.resize((256, 256), Image.Resampling.LANCZOS)
    out_bubble_path = res_dir / "ic_floating_bubble_premium.png"
    img_bubble_resized.save(out_bubble_path, "PNG")
    print(f"SUCCESS: Generated {out_bubble_path}")
    print("All premium assets processed successfully!")

if __name__ == "__main__":
    main()
