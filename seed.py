import urllib.request
import json
import ssl

def post_json(url, data):
    req = urllib.request.Request(url, data=json.dumps(data).encode('utf-8'),
                                 headers={'Content-Type': 'application/json'})
    try:
        with urllib.request.urlopen(req) as f:
            return json.loads(f.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        print("Error from " + url + ": " + e.read().decode('utf-8'))
        return None

def put_json(url, data):
    req = urllib.request.Request(url, data=json.dumps(data).encode('utf-8'),
                                 headers={'Content-Type': 'application/json'}, method='PUT')
    try:
        with urllib.request.urlopen(req) as f:
            return json.loads(f.read().decode('utf-8'))
    except urllib.error.HTTPError as e:
        print("Error from " + url + ": " + e.read().decode('utf-8'))
        return None

print('Starting data seeder...')
# 1. Create Restaurant
rest_data = {
    'name': 'Gà Rán Xì Trum (Test)',
    'description': 'Quán xì trum chuyên bán gà rán 24/7',
    'imageUrl': 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd',
    'rating': 4.9,
    'deliveryTime': 25,
    'deliveryFee': 12000.0
}
rest = post_json('http://localhost:8080/api/restaurants', rest_data)
rest_id = rest['id']
print("Created Restaurant ID: " + str(rest_id))

# 2. Set hours (Monday -> Sunday)
days = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY']
for d in days:
    hours_data = {
        'dayOfWeek': d,
        'openTime': '00:00:00',
        'closeTime': '23:59:59',
        'closed': False
    }
    put_json('http://localhost:8080/api/restaurants/' + str(rest_id) + '/hours/' + d, hours_data)

print('Hours configured successfully.')

# 3. Create 2 foods
food1 = {
    'restaurant': {'id': rest_id},
    'name': 'Đùi Gà Giòn Xối Mỡ',
    'description': 'Đùi gà chiên giòn sốt cay rắc mè rang',
    'price': 45000.0,
    'imageUrl': 'https://images.unsplash.com/photo-1562967914-608f82629710',
    'category': 'Gà rán',
    'available': True,
    'bestSeller': True,
    'new': False,
    'rating': 4.8
}

food2 = {
    'restaurant': {'id': rest_id},
    'name': 'Khoai Tây Lắc Phô Mai',
    'description': 'Khoai tây chiên cắt gợn sóng rắc bột phô mai truyền thống',
    'price': 25000.0,
    'imageUrl': 'https://images.unsplash.com/photo-1576107232684-1279f3908594',
    'category': 'Ăn vặt',
    'available': True,
    'bestSeller': False,
    'new': True,
    'rating': 4.5
}
post_json('http://localhost:8080/api/foods', food1)
post_json('http://localhost:8080/api/foods', food2)
print('Foods created successfully!')
