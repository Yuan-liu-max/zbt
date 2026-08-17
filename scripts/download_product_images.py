#!/usr/bin/env python3
"""
批量下载珠宝商品图片 — 支持 Pexels API / Unsplash 源 / 本地生成 SQL

用法:
    # Pexels API（推荐，图片质量高）
    python scripts/download_product_images.py --source pexels --pexels-key YOUR_API_KEY

    # 无 API Key？用 loremflickr 免费源（无需注册）
    python scripts/download_product_images.py --source loremflickr

    # 仅生成 SQL UPDATE 语句（配合已有图片使用）
    python scripts/download_product_images.py --generate-sql

    # 下载后输出 SQL
    python scripts/download_product_images.py --source pexels --pexels-key KEY --sql

免费 API Key 获取:
    Pexels: https://www.pexels.com/api/  （免费注册，200次/小时）

输出:
    - 图片文件 → data/files/images/products/product_XXXX.jpg
    - 访问路径 → /files/static/images/products/product_XXXX.jpg
    - SQL 文件 → scripts/product_image_updates.sql
"""

import argparse
import json
import os
import sys
import time
import urllib.request
import urllib.error
from pathlib import Path

# ============================================================
# 产品定义 — 与 StartupMigration.java 中的产品一一对应
# ============================================================
PRODUCTS = [
    (1001, "P-GOLD-001", "足金花开富贵手镯", "黄金", "gold bracelet bangle jewelry"),
    (1002, "P-GOLD-002", "古法黄金传承吊坠", "黄金", "gold pendant necklace traditional"),
    (1003, "P-DIAMOND-001", "1克拉六爪钻戒", "钻石", "diamond solitaire engagement ring"),
    (1004, "P-DIAMOND-002", "30分钻石项链", "钻石", "diamond necklace pendant white gold"),
    (1005, "P-JADE-001", "冰种翡翠手镯", "翡翠", "jade bangle bracelet green"),
    (1006, "P-JADE-002", "糯种翡翠平安扣", "翡翠", "jade pendant charm green"),
    (1007, "P-PEARL-001", "南洋金珠项链", "珍珠", "golden south sea pearl necklace luxury"),
    (1008, "P-PEARL-002", "淡水珍珠耳钉", "珍珠", "freshwater pearl earrings stud white"),
    (1009, "P-KGOLD-001", "18K金时尚锁骨链", "K金", "gold chain necklace fashion delicate"),
    (1010, "P-KGOLD-002", "18K玫瑰金戒指", "K金", "rose gold ring elegant fashion"),
    (1011, "P-PLAT-001", "铂金情侣对戒", "铂金", "platinum couple wedding rings pair"),
    (1012, "P-PLAT-002", "铂金素圈戒指", "铂金", "platinum band ring simple minimal"),
    (1013, "P-SILVER-001", "925银镶锆石手链", "银饰", "silver bracelet cz zircon chain"),
    (1014, "P-SILVER-002", "藏银民族风耳环", "银饰", "silver ethnic bohemian earrings"),
    (1015, "P-GOLD-003", "3D硬金生肖转运珠", "黄金", "gold bead charm pendant zodiac"),
    (1016, "P-JADE-003", "和田玉平安牌", "翡翠", "nephrite jade pendant white hetian"),
    (1017, "P-GOLD-004", "足金戒指男款", "黄金", "gold ring men signet masculine"),
    (1018, "P-DIAMOND-003", "钻石耳钉一对", "钻石", "diamond stud earrings pair white gold"),
    (1019, "P-PEARL-003", "大溪地黑珍珠吊坠", "珍珠", "black tahitian pearl pendant dark"),
    (1020, "P-KGOLD-003", "18K金编织手镯", "K金", "gold woven braided bracelet bangle"),
]

# 项目根目录 (脚本所在目录的上一级)
PROJECT_ROOT = Path(__file__).resolve().parent.parent
OUTPUT_DIR = PROJECT_ROOT / "data" / "files" / "images" / "products"
SQL_OUTPUT = Path(__file__).resolve().parent / "product_image_updates.sql"


def download_file(url: str, dest: Path, timeout: int = 30) -> bool:
    """下载文件到指定路径，返回是否成功"""
    try:
        req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
        with urllib.request.urlopen(req, timeout=timeout) as resp:
            if resp.status != 200:
                print(f"  HTTP {resp.status}")
                return False
            dest.write_bytes(resp.read())
        return True
    except urllib.error.URLError as e:
        print(f"  网络错误: {e.reason}")
        return False
    except Exception as e:
        print(f"  下载失败: {e}")
        return False


# ============================================================
# Pexels API 源
# ============================================================
def download_pexels(api_key: str, product_id: int, keyword: str, name: str) -> str | None:
    """通过 Pexels API 搜索并下载一张图片，返回本地访问路径"""
    search_url = f"https://api.pexels.com/v1/search?query={urllib.request.quote(keyword)}&per_page=5&orientation=square"
    req = urllib.request.Request(search_url, headers={"Authorization": api_key})

    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            data = json.loads(resp.read())
    except Exception as e:
        print(f"  Pexels API 错误: {e}")
        return None

    photos = data.get("photos", [])
    if not photos:
        print(f"  未找到匹配图片: {keyword}")
        return None

    # 取第一张图片的 medium (400px) 尺寸
    photo = photos[0]
    image_url = photo["src"]["medium"]
    photographer = photo["photographer"]
    print(f"  选中: {photographer} — {image_url[:60]}...")

    dest = OUTPUT_DIR / f"product_{product_id}.jpg"
    if download_file(image_url, dest):
        print(f"  已保存: {dest}")
        return f"/files/static/images/products/product_{product_id}.jpg"
    return None


# ============================================================
# LoremFlickr 免费源（无需 API Key）
# ============================================================
def download_loremflickr(product_id: int, keyword: str, name: str) -> str | None:
    """通过 loremflickr.com 获取随机图片（免费，无需注册）"""
    # loremflickr 会根据关键词返回随机 Flickr 图片
    # 格式: https://loremflickr.com/{width}/{height}/{keywords}
    encoded = ",".join(keyword.replace(" ", ",").split(",")[:5])
    image_url = f"https://loremflickr.com/400/400/{encoded}"

    print(f"  请求: {image_url}")
    dest = OUTPUT_DIR / f"product_{product_id}.jpg"
    if download_file(image_url, dest):
        # 检查是否是有效图片（loremflickr 404 时返回文本）
        if dest.stat().st_size < 1000:
            print(f"  图片无效（文件过小），跳过")
            dest.unlink(missing_ok=True)
            return None
        print(f"  已保存: {dest}")
        return f"/files/static/images/products/product_{product_id}.jpg"
    return None


# ============================================================
# SQL 生成
# ============================================================
def generate_sql():
    """生成 UPDATE SQL 语句"""
    lines = [
        "-- ============================================================",
        "-- 商品图片 URL 批量更新",
        "-- 生成时间: " + time.strftime("%Y-%m-%d %H:%M:%S"),
        "-- 用法: 在 MySQL 中执行此文件",
        "-- ============================================================",
        "",
    ]
    for pid, code, name, category, keyword in PRODUCTS:
        path = f"/files/static/images/products/product_{pid}.jpg"
        lines.append(
            f"UPDATE product SET image_url = '{path}' "
            f"WHERE id = {pid};  -- {name} ({category})"
        )
    lines.append("")
    SQL_OUTPUT.write_text("\n".join(lines), encoding="utf-8")
    print(f"\nSQL 已生成: {SQL_OUTPUT}")
    print(f"共 {len(PRODUCTS)} 条 UPDATE 语句")


# ============================================================
# 主流程
# ============================================================
def main():
    parser = argparse.ArgumentParser(
        description="批量下载珠宝商品图片",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
示例:
  python scripts/download_product_images.py --source loremflickr
  python scripts/download_product_images.py --source pexels --pexels-key abc123
  python scripts/download_product_images.py --source pexels --pexels-key abc123 --sql
  python scripts/download_product_images.py --generate-sql
        """,
    )
    parser.add_argument(
        "--source", choices=["pexels", "loremflickr"], default="loremflickr",
        help="图片源: pexels (需要API Key) | loremflickr (免费, 默认)"
    )
    parser.add_argument("--pexels-key", help="Pexels API Key (https://www.pexels.com/api/)")
    parser.add_argument("--sql", action="store_true", help="下载后同时生成 SQL 更新文件")
    parser.add_argument("--generate-sql", action="store_true", help="仅生成 SQL 更新文件")
    parser.add_argument("--delay", type=float, default=1.0, help="请求间隔秒数 (默认1秒)")

    args = parser.parse_args()

    # 仅生成 SQL
    if args.generate_sql:
        generate_sql()
        return

    # 验证 Pexels 参数
    if args.source == "pexels" and not args.pexels_key:
        print("错误: --source pexels 需要 --pexels-key YOUR_API_KEY")
        print("免费注册: https://www.pexels.com/api/")
        print("或使用免费源: --source loremflickr")
        sys.exit(1)

    # 创建输出目录
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    print(f"图片输出目录: {OUTPUT_DIR}")
    print(f"图片来源: {args.source}")
    print(f"共需处理: {len(PRODUCTS)} 个商品\n")

    successful = 0
    for pid, code, name, category, keyword in PRODUCTS:
        print(f"[{pid}] {name} ({category}) → {keyword}")

        if args.source == "pexels":
            result = download_pexels(args.pexels_key, pid, keyword, name)
        else:
            result = download_loremflickr(pid, keyword, name)

        if result:
            successful += 1

        # 限速
        time.sleep(args.delay)

    print(f"\n{'='*50}")
    print(f"完成: {successful}/{len(PRODUCTS)} 张图片已下载")
    print(f"存储位置: {OUTPUT_DIR}")
    print(f"访问路径: /files/static/images/products/product_XXXX.jpg")
    print(f"{'='*50}")

    # 生成 SQL
    if args.sql or successful > 0:
        generate_sql()


if __name__ == "__main__":
    main()
